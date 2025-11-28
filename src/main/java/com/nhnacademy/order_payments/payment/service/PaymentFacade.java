package com.nhnacademy.order_payments.payment.service;

import com.nhnacademy.order_payments.dto.request.CancelRequest;
import com.nhnacademy.order_payments.dto.request.ConfirmRequest;
import com.nhnacademy.order_payments.dto.response.CancelResponse;
import com.nhnacademy.order_payments.dto.response.ConfirmResponse;
import com.nhnacademy.order_payments.entity.Order;
import com.nhnacademy.order_payments.entity.Payment;
import com.nhnacademy.order_payments.entity.PaymentHistory;
import com.nhnacademy.order_payments.exception.BusinessException;
import com.nhnacademy.order_payments.model.PaymentEventType;
import com.nhnacademy.order_payments.model.PaymentMethod;
import com.nhnacademy.order_payments.payment.provider.PaymentProvider;
import com.nhnacademy.order_payments.repository.OrderRepository;
import com.nhnacademy.order_payments.repository.PaymentHistoryRepository;
import com.nhnacademy.order_payments.repository.PaymentRepository;
import com.nhnacademy.order_payments.repository.PointHistoryRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Slf4j
@Service
public class PaymentFacade {
    private final OrderRepository orders;
    private final PaymentRepository payments;
    private final PaymentHistoryRepository paymentHistories;
    private final PaymentProvider provider;

    public PaymentFacade(OrderRepository orders,
                         PaymentRepository payments,
                         PaymentHistoryRepository paymentHistories,
                         PaymentProvider provider) {
        this.orders = orders;
        this.payments = payments;
        this.paymentHistories = paymentHistories;
        this.provider = provider;
    }

    /** 결제 승인 */
    @Transactional
    public ConfirmResponse confirm(ConfirmRequest req) {
        // 결제 요청 로그
        log.info("[PAYMENT CONFIRM START] orderId={}, paymentKey={}, amount={}",
                req.orderId(), req.paymentKey(), req.amount());

        // 1. 주문 조회(문자만 오면 주문번호로 찾음)
        Order order = findOrder(req.orderId());
        log.info("[PAYMENT CONFIRM] found order, orderNumber={}, totalPrice={}",
                order.getOrderNumber(), order.getTotalPrice());

        // 2. 금액 검증(totalPrice = 최종 결제금)
        if (order.getTotalPrice() == null || !order.getTotalPrice().equals(req.amount())) {
            log.warn("[PAYMENT CONFIRM AMOUNT_MISMATCH] orderNumber={}, expectedAmount={}, requestAmount={}",
                    order.getOrderNumber(), order.getTotalPrice(), req.amount());
            throw new BusinessException("AMOUNT_MISMATCH", "결제금액이 총 주문 금액과 다릅니다.");
        }

        // 3. 멱등성 보장(이미 결제가 있으면(중복요청) 성공 응답)
        Payment existing = payments.findByOrder(order).orElse(null);
        if (existing != null) {
            log.info("[PAYMENT CONFIRM DUPLICATE] orderNumber={}, paymentKey={}, method={}, approvedAt={}",
                    order.getOrderNumber(), existing.getPaymentKey(),
                    existing.getPaymentMethod(), existing.getApprovedAt());

            return new ConfirmResponse(
                    String.valueOf(order.getOrderNumber()),
                    "PAID",
                    existing.getApprovedAt(),
                    existing.getPaymentMethod().name()
            );
        }

        // 4. 승인 (Toss PG 승인 API 호출)
        var result = provider.approve(new PaymentProvider.ApproveCommand(
                req.orderId(), req.paymentKey(), req.amount()));

        log.info("[PAYMENT CONFIRM PG_SUCCESS] orderNumber={}, provider={}, method={}, approvedAtIso={}",
                order.getOrderNumber(), result.provider(), result.method(), result.approvedAtIso());

        //마지막에 추가한것
        PaymentMethod payMethod = PaymentMethod.fromTossMethod(result.method());

        OffsetDateTime approvedAt = null;
        if (result.approvedAtIso() != null && !result.approvedAtIso().isBlank()) {
            try {
                approvedAt = OffsetDateTime.parse(result.approvedAtIso());
            } catch (Exception e) {
                log.warn("[PAYMENT CONFIRM PARSE_APPROVED_AT_FAIL] approvedAtIso={}", result.approvedAtIso(), e);
            }
        }

        // 5. 결제 저장
        Payment p = Payment.builder()
                .order(order)
                .paymentCost(req.amount())
                .paymentKey(req.paymentKey())
                .paymentMethod(payMethod)
                .pgProvider(result.provider())
                .cardIssuerCode(null)
                .build();


        if (approvedAt != null) {
            p.setApprovedAt(approvedAt);
        }

        payments.save(p);
        log.info("[PAYMENT CONFIRM SAVED] orderNumber={}, paymentKey={}, method={}, approvedAt={}",
                order.getOrderNumber(), p.getPaymentKey(), p.getPaymentMethod(), p.getApprovedAt());

        // 5-1. 결제 이력 저장
        paymentHistories.save(PaymentHistory.builder()
                .payment(p)
                .paymentId(null)
                .eventType(PaymentEventType.APPROVE)
                .amount(req.amount())
                .reason(null)
                .paymentTime(LocalDateTime.now())
                .build());

        log.info("[PAYMENT HISTORY SAVED] orderNumber={}, eventType={}, amount={}",
                order.getOrderNumber(), PaymentEventType.APPROVE, req.amount());

        // 6. 응답 반환
        ConfirmResponse response = new ConfirmResponse(
                String.valueOf(order.getOrderNumber()),
                "PAID",
                approvedAt,
                payMethod.name()
        );

        log.info("[PAYMENT CONFIRM END] orderNumber={}, status={}", order.getOrderNumber(), response.status());
        return response;
    }

    /** 결제 취소(전액/부분 취소) */
    @Transactional
    public CancelResponse cancel(CancelRequest req) {
        // 결제 요청 로그
        log.info("[PAYMENT CANCEL START] orderId={}, paymentKey={}, cancelAmount={}, reason={}",
                req.orderId(), req.paymentKey(), req.cancelAmount(), req.reason());

        // 1. 주문 & 결제 조회
        Order order = findOrder(req.orderId());
        Payment payment = payments.findByOrder(order)
                .orElseThrow(() -> new BusinessException("PAYMENT_NOT_FOUND", "해당 주문의 결제 내역이 없습니다."));

        log.info("[PAYMENT CANCEL] found order/payment, orderNumber={}, paymentKey={}, paidAmount={}",
                order.getOrderNumber(), payment.getPaymentKey(), payment.getPaymentCost());

        // 2. 취소 금액 결정(기본 = 전액)
        int cancelAmount = (req.cancelAmount() == null) ? payment.getPaymentCost() : req.cancelAmount();
        if (cancelAmount <= 0 || cancelAmount > payment.getPaymentCost()) {
            log.warn("[PAYMENT CANCEL INVALID_AMOUNT] orderNumber={}, cancelAmount={}, paidAmount={}",
                    order.getOrderNumber(), cancelAmount, payment.getPaymentCost());
            throw new BusinessException("INVALID_CANCEL_AMOUNT", "취소 금액이 올바르지 않습니다.");
        }

        // 3. PG 취소 호출 (토스)
        var result = provider.cancel(new PaymentProvider.CancelCommand(
                req.orderId(), req.paymentKey(), cancelAmount, req.reason()
        ));

        log.info("[PAYMENT CANCEL PG_SUCCESS] orderNumber={}, provider={}, method={}, canceledAtIso={}",
                order.getOrderNumber(), result.provider(), result.method(), result.canceledAtIso());

        // 5. 결제 이력 저장(취소/부분취소)
        PaymentEventType eventType =
                (cancelAmount == payment.getPaymentCost())
                        ? PaymentEventType.CANCEL
                        : PaymentEventType.PARTIAL_CANCEL;

        paymentHistories.save(PaymentHistory.builder()
                .payment(payment)
                .paymentId(null)
                .eventType(eventType)
                .amount(cancelAmount)
                .reason(req.reason())
                .paymentTime(LocalDateTime.now())
                .build());

        log.info("[PAYMENT HISTORY SAVED] orderNumber={}, eventType={}, amount={}",
                order.getOrderNumber(), eventType, cancelAmount);

        // 6. 응답 반환
        CancelResponse response = new CancelResponse(
                String.valueOf(order.getOrderNumber()),
                "CANCELED",
                result.canceledAtIso(), // ISO-8601
                result.method()
        );

        log.info("[PAYMENT CANCEL END] orderNumber={}, status={}", order.getOrderNumber(), response.status());
        return response;
    }

    private Order findOrder(String idOrNo) {
        // 1) 토스용 orderId + 타임스탬프가 붙어오면 타임스탬프를 떼고 줌("000001-1732..." -> "000001")
        String normalized = idOrNo;
        int dashIndex = idOrNo.indexOf('-');
        if (dashIndex > 0) {
            normalized = idOrNo.substring(0, dashIndex);
        }

        // 2) 앞부분을 숫자로 파싱
        final long n;
        try {
            n = Long.parseLong(normalized); // "000001" -> 1
        } catch (NumberFormatException e) {
            throw new BusinessException("ORDER_ID_FORMAT", "숫자 형태의 주문 식별자가 필요합니다.");
        }

        // 3) PK 또는 주문번호로 조회
        return orders.findById(n)
                .or(() -> orders.findByOrderNumber(n))
                .orElseThrow(() -> new BusinessException("ORDER_NOT_FOUND", "주문 없음"));
    }

}
