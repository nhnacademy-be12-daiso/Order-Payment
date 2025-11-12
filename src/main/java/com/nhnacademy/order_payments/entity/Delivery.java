package com.nhnacademy.order_payments.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "Deliveries")
public class Delivery {

    @Id
    private Long deliveryId;

    @OneToOne
    private Order orders;

    private String receiverName;
    private LocalDateTime estimatedAt;
    private LocalDateTime completeAt;
    private Integer deliveryFee;
}
