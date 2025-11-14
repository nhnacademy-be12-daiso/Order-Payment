package com.nhnacademy.order_payments.entity;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "Carts")
@NoArgsConstructor
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartId;

    @Column(unique = true)
    private Long userCreatedId; // 회원에서 뽑아와야 함
    private String cartName; // 이게 필요함?

    public Cart(Long ordererId) {
    }
}
