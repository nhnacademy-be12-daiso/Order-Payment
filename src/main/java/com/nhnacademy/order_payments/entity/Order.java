/*
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 * + Copyright 2025. NHN Academy Corp. All rights reserved.
 * + * While every precaution has been taken in the preparation of this resource,  assumes no
 * + responsibility for errors or omissions, or for damages resulting from the use of the information
 * + contained herein
 * + No part of this resource may be reproduced, stored in a retrieval system, or transmitted, in any
 * + form or by any means, electronic, mechanical, photocopying, recording, or otherwise, without the
 * + prior written permission.
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 */

package com.nhnacademy.order_payments.entity;

import com.nhnacademy.order_payments.dto.order.OrderSummaryDto;
import com.nhnacademy.order_payments.model.Grade;
import com.nhnacademy.order_payments.model.OrderStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "Orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @Setter
    @ManyToOne
    @JoinColumn(name = "guest_id", nullable = true)
    private GuestOrderer guestOrderer;

    @Setter
    @Column(name = "user_created_id", nullable = true)
    private Long userId;

    @Column(name = "order_number", unique = true)
    private Long orderNumber; // pk랑 무슨 차이가 있는지?

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "order_status")
    private OrderStatus orderStatus;// enum으로 독립

    @Column(name = "order_date")
    private ZonedDateTime orderDate;

    @Column(name = "orderer_name", nullable = false)
    private String ordererName;

    @Column(name = "total_price")
    private Long totalPrice;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "email")
    private String email;

//    @Column(name = "grade_name")
//    private Grade grade;

    public Order(String ordererName, Long totalPrice, String phoneNumber, String email) {
        this.ordererName = ordererName;
        this.orderNumber = generateOrderNumber();
        this.orderDate = ZonedDateTime.now();
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.totalPrice = totalPrice;
        this.orderStatus = OrderStatus.PENDING;
//        this.grade = grade;
    }

    public Order(OrderSummaryDto dto) {
        this.ordererName = dto.ordererSummaryDto().ordererName();
        this.orderNumber = generateOrderNumber();
        this.orderDate = ZonedDateTime.now();
        this.phoneNumber = dto.ordererSummaryDto().ordererPhoneNumber();
        this.email = dto.ordererSummaryDto().ordererEmail();
        this.totalPrice = dto.totalPrice();
        this.orderStatus = OrderStatus.PENDING;
//        this.grade =
    }

    public Long generateOrderNumber() {
        long timestamp = Instant.now().toEpochMilli();
        long randomPart = ThreadLocalRandom.current().nextLong(100000L);
        long orderNumber = timestamp * 100000L + randomPart;
        return Math.abs(orderNumber); // 음수 방지
    }

    @Setter
    @OneToMany(mappedBy = "order", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<OrderDetail> orderDetailList = new ArrayList<>();

    public void addOrderDetail(OrderDetail orderDetail) {
        this.orderDetailList.add(orderDetail);
        orderDetail.setOrder(this);
    }

    @Setter
    @OneToOne(mappedBy = "order")
    private Delivery delivery;
}
