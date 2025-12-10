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
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "DeliveryOrderDetails")
public class DeliveryOrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delovery_order_detail_id")  // 오타
    private Long id;

    @Setter
    @ManyToOne
    @JoinColumn(name = "delovery_detail_id")    // 오타
    private DeliveryDetail deliveryDetail;

    @Setter
    @ManyToOne
    @JoinColumn(name = "order_detail_id")
    private OrderDetail orderDetail;

    @Setter
    private Integer quantity;

    public DeliveryOrderDetail(Integer quantity) {
        this(null, null, quantity);
    }

    public DeliveryOrderDetail(DeliveryDetail deliveryDetail, OrderDetail orderDetail, Integer quantity) {
        this.deliveryDetail = deliveryDetail;
        this.orderDetail = orderDetail;
        this.quantity = quantity;
    }
}
