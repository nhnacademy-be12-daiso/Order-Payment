package com.nhnacademy.order_payments.service.payment;

import com.nhnacademy.order_payments.dto.request.CancelRequest;
import com.nhnacademy.order_payments.dto.request.ConfirmRequest;
import com.nhnacademy.order_payments.dto.request.FailRequest;
import com.nhnacademy.order_payments.dto.request.RefundRequest;
import com.nhnacademy.order_payments.dto.response.CancelResponse;
import com.nhnacademy.order_payments.dto.response.ConfirmResponse;
import com.nhnacademy.order_payments.dto.response.PaymentHistoryResponse;
import com.nhnacademy.order_payments.dto.response.RefundResponse;   // ✅ 추가
import com.nhnacademy.order_payments.entity.Order;
import com.nhnacademy.order_payments.entity.Payment;
import com.nhnacademy.order_payments.entity.PaymentHistory;
import com.nhnacademy.order_payments.exception.BusinessException;
import com.nhnacademy.order_payments.model.PaymentEventType;
import com.nhnacademy.order_payments.model.PaymentMethod;
import com.nhnacademy.order_payments.provider.PaymentProvider;
import com.nhnacademy.order_payments.repository.OrderRepository;
import com.nhnacademy.order_payments.repository.PaymentHistoryRepository;
import com.nhnacademy.order_payments.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

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
    public ConfirmResponse confirm(Long userId, ConfirmRequest req) {
        log.info("[PAYMENT CONFIRM START] userId={}, orderId={}, paymentKey={}, amount={}",
                userId, req.orderId(), req.paymentKey(), req.amount());

        // 1. 주문 조회
        Order order = findOrder(req.orderId());
        log.info("[PAYMENT CONFIRM] found order, orderNumber={}, totalPrice={}",
                order.getOrderNumber(), order.getTotalPrice());

        // 2. 금액 검증
        if (order.getTotalPrice() == null || !order.getTotalPrice().equals(req.amount())) {
            log.warn("[PAYMENT CONFIRM AMOUNT_MISMATCH] orderNumber={}, expectedAmount={}, requestAmount={}",
                    order.getOrderNumber(), order.getTotalPrice(), req.amount());
            throw new BusinessException("AMOUNT_MISMATCH", "결제금액이 총 주문 금액과 다릅니다.");
        }

        // 3. 멱등성 보장
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

        // Toss method → enum 매핑
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

        // 5-1. 결제 이력 저장 (APPROVE)
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
    public CancelResponse cancel(Long userId, CancelRequest req) {
        log.info("[PAYMENT CANCEL START] userId={}, orderId={}, paymentKey={}, cancelAmount={}, reason={}",
                userId, req.orderId(), req.paymentKey(), req.cancelAmount(), req.reason());

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

        // 3. PG 취소 호출
        var result = provider.cancel(new PaymentProvider.CancelCommand(
                req.orderId(), req.paymentKey(), cancelAmount, req.reason()
        ));

        log.info("[PAYMENT CANCEL PG_SUCCESS] orderNumber={}, provider={}, method={}, canceledAtIso={}",
                order.getOrderNumber(), result.provider(), result.method(), result.canceledAtIso());

        // 4. 결제 이력 저장(취소/부분취소)
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

        // 5. 응답 반환
        CancelResponse response = new CancelResponse(
                String.valueOf(order.getOrderNumber()),
                "CANCELED",
                result.canceledAtIso(),
                result.method()
        );

        log.info("[PAYMENT CANCEL END] orderNumber={}, status={}", order.getOrderNumber(), response.status());
        return response;
    }

    /** 환불 처리 (REFUND 이력용) */
    @Transactional
    public RefundResponse refund(Long userId, @Valid RefundRequest req) {   // ✅ 리턴 타입 변경
        log.info("[PAYMENT REFUND START] userId={}, orderId={}, paymentKey={}, cancelAmount={}, reason={}",
                userId, req.orderId(), req.paymentKey(), req.cancelAmount(), req.reason());

        // 1. 주문 & 결제 조회
        Order order = findOrder(req.orderId());
        Payment payment = payments.findByOrder(order)
                .orElseThrow(() -> new BusinessException("PAYMENT_NOT_FOUND", "해당 주문의 결제 내역이 없습니다."));

        log.info("[PAYMENT REFUND] found order/payment, orderNumber={}, paymentKey={}, paidAmount={}",
                order.getOrderNumber(), payment.getPaymentKey(), payment.getPaymentCost());

        // 2. 환불 금액 결정(없으면 전액)
        int cancelAmount = (req.cancelAmount() == null) ? payment.getPaymentCost() : req.cancelAmount();
        if (cancelAmount <= 0 || cancelAmount > payment.getPaymentCost()) {
            log.warn("[PAYMENT REFUND INVALID_AMOUNT] orderNumber={}, cancelAmount={}, paidAmount={}",
                    order.getOrderNumber(), cancelAmount, payment.getPaymentCost());
            throw new BusinessException("INVALID_CANCEL_AMOUNT", "환불 금액이 올바르지 않습니다.");
        }

        // 3. 토스 취소(환불) 호출
        var result = provider.cancel(new PaymentProvider.CancelCommand(
                req.orderId(), req.paymentKey(), cancelAmount, req.reason()
        ));

        log.info("[PAYMENT REFUND PG_SUCCESS] orderNumber={}, provider={}, method={}, canceledAtIso={}",
                order.getOrderNumber(), result.provider(), result.method(), result.canceledAtIso());

        // 4. 결제 이력 저장 – REFUND
        paymentHistories.save(PaymentHistory.builder()
                .payment(payment)
                .paymentId(null)
                .eventType(PaymentEventType.REFUND)
                .amount(cancelAmount)
                .reason(req.reason())
                .paymentTime(LocalDateTime.now())
                .build());

        log.info("[PAYMENT HISTORY SAVED] orderNumber={}, eventType={}, amount={}",
                order.getOrderNumber(), PaymentEventType.REFUND, cancelAmount);

        // 5. 응답 반환
        RefundResponse response = new RefundResponse(   // ✅ RefundResponse 사용
                String.valueOf(order.getOrderNumber()),
                "REFUNDED",
                result.canceledAtIso(),    // refundedAt 필드에 ISO 문자열
                result.method()
        );

        log.info("[PAYMENT REFUND END] orderNumber={}, status={}", order.getOrderNumber(), response.status());
        return response;
    }

    /** 결제 실패 기록 (FAIL 이력용) */
    @Transactional
    public void fail(FailRequest req) {
        log.info("[PAYMENT FAIL START] orderId={}, paymentKey={}, amount={}, errorCode={}, errorMessage={}",
                req.orderId(), req.paymentKey(), req.amount(), req.errorCode(), req.errorMessage()); // ✅ record 스타일

        // 실패는 실제 Payment 엔티티가 없을 수 있어서 payment=null 로 저장
        paymentHistories.save(PaymentHistory.builder()
                .payment(null)
                .paymentId(null)
                .eventType(PaymentEventType.FAIL)
                .amount(req.amount())
                .reason("[" + req.errorCode() + "] " + req.errorMessage())
                .paymentTime(LocalDateTime.now())
                .build());

        log.info("[PAYMENT FAIL HISTORY SAVED] orderId={}, amount={}", req.orderId(), req.amount());
    }

    /** 주문 조회 유틸 – 토스용 orderId(타임스탬프 붙은 형태)까지 처리 */
    private Order findOrder(String idOrNo) {
        String normalized = idOrNo;
        int dashIndex = idOrNo.indexOf('-');
        if (dashIndex > 0) {
            normalized = idOrNo.substring(0, dashIndex);
        }

        final long n;
        try {
            n = Long.parseLong(normalized);
        } catch (NumberFormatException e) {
            throw new BusinessException("ORDER_ID_FORMAT", "숫자 형태의 주문 식별자가 필요합니다.");
        }

        return orders.findById(n)
                .or(() -> orders.findByOrderNumber(n))
                .orElseThrow(() -> new BusinessException("ORDER_NOT_FOUND", "주문 없음"));
    }
    /** 주문에 대한 결제 히스토리를 조회 */
    @Transactional
    public List<PaymentHistoryResponse> getHistory(Long userId, String orderIdOrNumber) {
        // 1. 주문 찾기 (001001-타임스탬프 형태도 처리해주는 기존 findOrder 재사용)
        Order order = findOrder(orderIdOrNumber);

        // (옵션) 멤버일 경우, 해당 주문의 소유자인지 체크하는 로직을 나중에 추가할 수 있음
        // TODO: userId와 order의 사용자 매핑 검사 (팀과 상의해서)

        Long orderNumber = order.getOrderNumber();

        // 2. 해당 주문의 PaymentHistory 목록 조회
        List<PaymentHistory> histories =
                paymentHistories.findByPaymentOrderOrderNumberOrderByPaymentTimeAsc(orderNumber);

        // 3. 엔티티 -> DTO 변환
        return histories.stream()
                .map(h -> new PaymentHistoryResponse(
                        h.getEventType(),
                        h.getAmount(),
                        h.getReason(),
                        h.getPaymentTime(),
                        h.getPayment() != null && h.getPayment().getPaymentMethod() != null
                                ? h.getPayment().getPaymentMethod().name()
                                : null
                ))
                .toList();
    }
}
