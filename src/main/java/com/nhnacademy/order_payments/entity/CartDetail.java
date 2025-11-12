package com.nhnacademy.order_payments.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "CartDetails")
public class CartDetail {

    @Id
    private Long cartDetailId;

    @ManyToOne
    private Cart cart;

    private Long bookId; // 도서에서 뽑아와야 함
    private Long quantity;
}
