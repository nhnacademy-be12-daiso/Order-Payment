package com.nhnacademy.order_payments.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Table(name = "Carts")
public class Cart {
    @Id
    private Long cartId;

    @Column(unique = true)
    private Long userCreatedId; // 회원에서 뽑아와야 함
    private String cartName; // 이게 필요함?
}
