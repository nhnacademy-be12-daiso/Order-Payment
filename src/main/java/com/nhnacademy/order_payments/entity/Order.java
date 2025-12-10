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

import com.nhnacademy.order_payments.model.Grade;
import com.nhnacademy.order_payments.model.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.ZonedDateTime;
import java.util.List;
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
    private Integer totalPrice;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "email")
    private String email;

    @Column(name = "grade_name")
    private Grade grade;

    public Order(Long orderNumber, String ordererName, Integer totalPrice, String phoneNumber, String email,
                 Grade grade) {
        this.orderNumber = orderNumber;
        this.ordererName = ordererName;
        this.totalPrice = totalPrice;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.grade = grade;
        this.orderDate = ZonedDateTime.now();
    }

    @Setter
    @OneToMany(mappedBy = "order")
    private List<OrderDetail> orderDetailList;

    @Setter
    @OneToOne(mappedBy = "order")
    private Delivery delivery;
}
