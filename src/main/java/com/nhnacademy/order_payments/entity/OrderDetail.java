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

import com.nhnacademy.order_payments.model.OrderDetailStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "OrderDetails")
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_detail_id")
    private Long id;

    @Setter
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order; // 관계매핑 필요

    @Column(name = "book_id")
    private Long bookId; // 도서에서 뽑아와야 함

    private Long price;

    private Integer quantity;

    @Column(name = "packaging_id")
    private Long packagingId; // 저장만 함

    @Setter
    @Column(name = "order_detail_status")
    private OrderDetailStatus orderDetailStatus;

    public OrderDetail(Long bookId, Long price, Integer quantity, Long packagingId) {
        this.bookId = bookId;
        this.price = price;
        this.quantity = quantity;
        this.packagingId = packagingId;
        this.orderDetailStatus = OrderDetailStatus.PENDING;
    }

//    @Setter
//    @ManyToOne
//    @JoinColumn(name = "deliver_detail_id")
//    private DeliveryDetail deliveryDetail;

    @Setter
    @OneToMany(mappedBy = "orderDetail")
    private List<DeliveryOrderDetail> deliveryOrderDetails;

}
