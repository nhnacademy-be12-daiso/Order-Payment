package com.nhnacademy.order_payments.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "CartDetails")
@NoArgsConstructor
@Setter
@Getter
public class CartDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_detail_id")
    private Long cartDetailId;

    @ManyToOne
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @Column(name = "book_id")
    private Long bookId; // 도서에서 뽑아와야 함
    private Integer quantity;

//     도서에서 받아와야하는 것
//    private String title;
//    private int price;
    // ---> 반정규화

    public CartDetail(Cart cart, long bookId, int quantity) {
        this.cart = cart;
        this.bookId = bookId;
        this.quantity = quantity;
//        this.title = title;
//        this.price = price;
    }
}