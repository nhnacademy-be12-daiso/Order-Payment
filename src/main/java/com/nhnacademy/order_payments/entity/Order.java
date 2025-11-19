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
    private Long orderId;

    @ManyToOne
    @JoinColumn(name = "orderer_id", nullable = true)
    private Orderer orderer;

    private Long orderNumber; // pk랑 무슨 차이가 있는지?
    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;// enum으로 독립

    private String ordererName;
    private String deliverAddress;
    private String postalCode;
    private Integer totalPrice;
}
