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
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "cart_detail_seq_gen") // SEQUENCE 전략 지정 및 이름 부여
    @SequenceGenerator(
            name = "cart_detail_seq_gen",
            sequenceName = "CART_DETAIL_SEQ", // DB에 생성될 시퀀스 객체 이름
            allocationSize = 50 // 성능 최적화: 시퀀스 번호를 50개씩 미리 할당받음
    )
    @Column(name = "cart_detail_id")
    private Long cartDetailId;

    @ManyToOne
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @Column(name = "book_id")
    private Long bookId; // 도서에서 뽑아와야 함
    private Integer quantity;

    public CartDetail(Cart cart, long bookId, int quantity) {
        this.cart = cart;
        this.bookId = bookId;
        this.quantity = quantity;
    }
}