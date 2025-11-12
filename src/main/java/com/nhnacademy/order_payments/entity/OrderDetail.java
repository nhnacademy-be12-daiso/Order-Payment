package com.nhnacademy.order_payments.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "OrderDetails")
public class OrderDetail {

    @Id
    private Long orderDetailId;

    @ManyToOne
    private Order orders; // 관계매핑 필요

    private Long bookId; // 도서에서 뽑아와야 함
    private Integer price;
    private Integer quantity;
    private Long packaging_id; // 저장만 함


}
