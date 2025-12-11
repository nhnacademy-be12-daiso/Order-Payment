package com.nhnacademy.order_payments.entity;

import com.nhnacademy.order_payments.model.PaymentEventType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "PaymentHistories")
public class PaymentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_history_id")
    private Long paymentHistoryId;

    @ManyToOne
    @JoinColumn(name = "payment_id")
    private Payment payment;

    private Long paymentId;    // 결제 ID를 그대로 저장해두는 컬럼

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type")
    private PaymentEventType eventType; // 결제 상태(승인, 취소, 환불 등)

    @Column(name = "amount")
    private Long amount;

    @Column(name = "reason")// 금액
    private String reason;

    @Column(name = "payment_time")// 사유(취소, 환불 등)
    private LocalDateTime paymentTime; // 이력 남긴 시각

    @Builder
    public PaymentHistory(Payment payment,
                          Long paymentId,
                          PaymentEventType eventType,
                          Long amount,
                          String reason,
                          LocalDateTime paymentTime) {
        this.payment = payment;
        this.paymentId = paymentId;
        this.eventType = eventType;
        this.amount = amount;
        this.reason = reason;
        this.paymentTime = paymentTime;
    }
}
