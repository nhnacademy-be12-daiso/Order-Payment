package com.nhnacademy.order_payments.payment.service;

import com.nhnacademy.order_payments.dto.request.CancelRequest;
import com.nhnacademy.order_payments.dto.request.ConfirmRequest;
import com.nhnacademy.order_payments.dto.response.CancelResponse;
import com.nhnacademy.order_payments.dto.response.ConfirmResponse;
import com.nhnacademy.order_payments.entity.Order;
import com.nhnacademy.order_payments.entity.Payment;
import com.nhnacademy.order_payments.entity.PaymentHistory;
import com.nhnacademy.order_payments.entity.PointHistory;
import com.nhnacademy.order_payments.exception.BusinessException;
import com.nhnacademy.order_payments.model.PaymentEventType;
import com.nhnacademy.order_payments.model.PaymentMethod;
import com.nhnacademy.order_payments.payment.provider.PaymentProvider;
import com.nhnacademy.order_payments.repository.OrderRepository;
import com.nhnacademy.order_payments.repository.PaymentHistoryRepository;
import com.nhnacademy.order_payments.repository.PaymentRepository;
import com.nhnacademy.order_payments.repository.PointHistoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Service
public class PaymentFacade {
    private final OrderRepository orders;
    private final PaymentRepository payments;
    private final PointHistoryRepository points;
    private final PaymentHistoryRepository paymentHistories;
    private final PaymentProvider provider;

    public PaymentFacade(OrderRepository orders, PaymentRepository payments, PointHistoryRepository points, PaymentHistoryRepository paymentHistories, PaymentProvider provider) {
        this.orders = orders;
        this.payments = payments;
        this.points = points;
        this.paymentHistories = paymentHistories;
        this.provider = provider;
    }

    /** 결제 승인 */
    @Transactional
    public ConfirmResponse confirm(ConfirmRequest req) {
        // 1.주문 조회(문자만 오면 주문번호로 찾음)
        Order order = findOrder(req.orderId());

        // 2.금액 검증(total_Price = 최종 결제금)
        if (order.getTotalPrice() == null || !order.getTotalPrice().equals(req.amount())) {
            throw new BusinessException("AMOUNT_MISMATCH", "결제금액이 총 주문 금액과 다릅니다.");
        }

        // 3.멱등성 보장(이미 결제가 있으면(중복요청) 성공 응답)
        Payment existing = payments.findByOrder(order).orElse(null);
        if (existing != null) {
            return new ConfirmResponse(String.valueOf(order.getOrderNumber()), "PAID",
                    existing.getApprovedAt(),
                    existing.getPaymentMethod().name());
        }

        // 4.승인(현재는 Fake 나중에는 Toss)
        var result = provider.approve(new PaymentProvider.ApproveCommand(
                req.orderId(), req.paymentKey(), req.amount()));

        OffsetDateTime approvedAt = null;
        if (result.approvedAtIso() != null && !result.approvedAtIso().isBlank()) {
            approvedAt = OffsetDateTime.parse(result.approvedAtIso());
        }

        // 5.결제 저장
        Payment p = Payment.builder()
                .order(order)
                .paymentCost(req.amount())
                .paymentKey(req.paymentKey())
                .paymentMethod(PaymentMethod.valueOf(result.method()))
                .pgProvider(result.provider())
                .cardIssuerCode(null)
                .build();
        if (result.approvedAtIso() != null) p.setApprovedAt(OffsetDateTime.parse(result.approvedAtIso()));
        payments.save(p);

        // 5-1.결제 이력 저장
        paymentHistories.save(PaymentHistory.builder()
                .payment(p)
                .paymentId(null)
                .eventType(PaymentEventType.APPROVE)
                .amount(req.amount())
                .reason(null)
                .paymentTime(LocalDateTime.now())
                .build());

        // 6.포인트 사용 기록
        int usedPoint = req.usedPoint() == null ? 0 : req.usedPoint();
        if (usedPoint > 0) {
            points.save(PointHistory.builder().order(order).userCreated(0L).usedPoint(usedPoint).usedAt(LocalDateTime.now()).build()); //사용 포인트 존재시 포인트 이력 적재(회원 entity가 없어 userCreated=0L 임시 처리)
        }

        return new ConfirmResponse(String.valueOf(order.getOrderNumber()), "PAID", approvedAt, result.method());
    }

    /** 결제 취소(전액/부분 취소) */
    @Transactional
    public CancelResponse cancel(CancelRequest req) {
        // 1.주문 & 결제 조회
        Order order = findOrder(req.orderId());
        Payment payment = payments.findByOrder(order)
                .orElseThrow(() -> new BusinessException("PAYMENT_NOT_FOUND", "해당 주문의 결제 내역이 없습니다."));

        // 2.취소 금액 결정(기본 = 전액)
        int cancelAmount = (req.cancelAmount() == null) ? payment.getPaymentCost() : req.cancelAmount();
        if (cancelAmount <= 0 || cancelAmount > payment.getPaymentCost()) {
            throw new BusinessException("INVALID_CANCEL_AMOUNT", "취소 금액이 올바르지 않습니다.");
        }

        // 3.PG 취소 호출(현재 Fake)
        var result = provider.cancel(new PaymentProvider.CancelCommand(req.orderId(), req.paymentKey(), cancelAmount, req.reason()
        ));

        // 4.포인트 롤백(우선 음수로 기록) -> 현재 미정
        int rollbackPoint = 0;
        if (rollbackPoint != 0) {
            points.save(PointHistory.builder()
                    .order(order)
                    .userCreated(0L)
                    .usedPoint(-Math.abs(rollbackPoint))
                    .usedAt(LocalDateTime.now())
                    .build());
        }

        // 5.결제 이력 저장(취소/부분취소)
        PaymentEventType eventType = (cancelAmount == payment.getPaymentCost()) ? PaymentEventType.CANCEL : PaymentEventType.PARTIAL_CANCEL;

        paymentHistories.save(PaymentHistory.builder()
                .payment(payment)
                .paymentId(null)
                .eventType(eventType)
                .amount(cancelAmount)
                .reason(req.reason())
                .paymentTime(LocalDateTime.now())
                .build());

        // 6.응답만 반환(취소 이력은 추후 생각)
        return new CancelResponse(
                String.valueOf(order.getOrderNumber()),
                "CANCELED",
                result.canceledAtIso(), //ISO-8601
                result.method()
        );
    }

    private Order findOrder(String idOrNo) {
        final long n;
        try {
            n = Long.parseLong(idOrNo); //숫자가 아니면 실패
        } catch (NumberFormatException e) {
            throw new BusinessException("ORDER_ID_FORMAT", "숫자 형태의 주문 식별자가 필요합니다.");
        }

        // PK로 먼저 시도후, 없으면 주문번호로 시도
        return orders.findById(n)
                .or(() -> orders.findByOrderNumber(n))
                .orElseThrow(() -> new BusinessException("ORDER_NOT_FOUND", "주문 없음"));
    }
}

