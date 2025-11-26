package com.nhnacademy.order_payments.entity;

import com.nhnacademy.order_payments.model.PaymentEventType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "PaymentHistories")
public class PaymentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentHistoryId;

    @ManyToOne
    private Payment payment;

    private Long paymentId;

    @Enumerated(EnumType.STRING)
    private PaymentEventType eventType; //결제 상태(승인, 취소. 환불..)

    private Integer amount;
    private String reason;
    private LocalDateTime paymentTime;

    @Builder
    public PaymentHistory(Payment payment, Long paymentId, PaymentEventType eventType, Integer amount, String reason, LocalDateTime paymentTime) {
        this.payment = payment;
        this.paymentId = paymentId;
        this.eventType = eventType;
        this.amount = amount;
        this.reason = reason;
        this.paymentTime = paymentTime;
    }

}
