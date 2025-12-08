package com.nhnacademy.order_payments.entity;

import com.nhnacademy.order_payments.model.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "Orders")
public class Order {

    @Id
    @Column(name = "order_id")
    private Long orderId;

    @ManyToOne
    @JoinColumn(name = "orderer_id", nullable = true)
    private Orderer orderer;

    @Column(name = "order_number")
    private Long orderNumber; // pk랑 무슨 차이가 있는지?

    @Column(name = "order_date")
    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status")
    private OrderStatus orderStatus;// enum으로 독립

    @Column(name = "orderer_name")
    private String ordererName;

    @Column(name = "deliver_address")
    private String deliverAddress;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "total_price")
    private Integer totalPrice;
}