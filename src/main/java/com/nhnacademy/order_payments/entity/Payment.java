package com.nhnacademy.order_payments.entity;

import com.nhnacademy.order_payments.model.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "Payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @OneToOne
    @JoinColumn(name = "order_id", unique = true)
    private Order order;

    @Setter
    private Integer paymentCost;

    private String paymentKey;

    @Setter
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private String pgProvider;

    @Setter
    private String cardIssuerCode;

    @Setter
    private OffsetDateTime approvedAt; //결제가 승인된 시각

    @Builder
    public Payment(Order order, Integer paymentCost, String paymentKey,
                   PaymentMethod paymentMethod, String pgProvider, String cardIssuerCode) {
        this.order = order;
        this.paymentCost = paymentCost;
        this.paymentKey = paymentKey;
        this.paymentMethod= paymentMethod;
        this.pgProvider = pgProvider;
        this.cardIssuerCode = cardIssuerCode;
    }

}
