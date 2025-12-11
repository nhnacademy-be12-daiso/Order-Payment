package com.nhnacademy.order_payments.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Entity
@Table(name = "Carts")
@NoArgsConstructor
@Getter
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Long cartId;
    @Column(name = "user_id", unique = true)
    private Long userId; // JWT에서 파싱한 userId

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL)
    private List<CartDetail> details = new ArrayList<>();

    public Cart(Long userId) {
        this.userId = userId;
        log.info("장바구니가 생성되었습니다 : {}", userId);
    }
}
