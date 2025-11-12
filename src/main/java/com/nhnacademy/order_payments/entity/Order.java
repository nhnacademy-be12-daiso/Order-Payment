package com.nhnacademy.order_payments.entity;

import com.nhnacademy.order_payments.model.OrderStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "Orders")
public class Order {

    @Id
    private Long orderId;

    @ManyToOne
    private Orderer orderer;

    private Long orderNumber; // pk랑 무슨 차이가 있는지?
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;  // enum으로 독립
    private String ordererName;
    private String deliverAddress;
    private String postalCode;
    private Integer totalPrice;
}
