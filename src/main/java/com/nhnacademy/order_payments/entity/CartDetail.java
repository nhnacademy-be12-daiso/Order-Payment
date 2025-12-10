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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
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

    public CartDetail(Cart cart, Long bookId, Integer quantity) {
        this.cart = cart;
        this.bookId = bookId;
        this.quantity = quantity;
    }
}