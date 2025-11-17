package com.nhnacademy.order_payments.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "CartDetails")
@NoArgsConstructor
@Setter
public class CartDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartDetailId;

    @ManyToOne
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    private Long bookId; // 도서에서 뽑아와야 함
    private Integer quantity;

    // 도서에서 받아와야하는 것
    private String title;
    private int price;

    public CartDetail(Cart cart, long bookId, int quantity, String title, int price) {
        this.cart = cart;
        this.bookId = bookId;
        this.quantity = quantity;
        this.title = title;
        this.price = price;
    }
}