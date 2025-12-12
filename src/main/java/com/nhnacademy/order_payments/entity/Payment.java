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
    @Column(name = "payment_id") // payment_id 아닌가?
    private Long paymentId;

    @OneToOne
    @JoinColumn(name = "order_id", unique = true)
    private Order order;

    @Setter
    @Column(name = "payment_cost")
    private Long paymentCost;

    @Column(name = "payment_key")
    private String paymentKey;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @Column(name = "pg_provider")
    private String pgProvider;

    @Setter
    @Column(name = "card_issuer_code")
    private String cardIssuerCode;

    @Setter
    @Column(name = "approvedAt")
    private OffsetDateTime approvedAt; //결제가 승인된 시각

    @Builder
    public Payment(Order order, Long paymentCost, String paymentKey,
                   PaymentMethod paymentMethod, String pgProvider, String cardIssuerCode) {
        this.order = order;
        this.paymentCost = paymentCost;
        this.paymentKey = paymentKey;
        this.paymentMethod= paymentMethod;
        this.pgProvider = pgProvider;
        this.cardIssuerCode = cardIssuerCode;
    }

}
