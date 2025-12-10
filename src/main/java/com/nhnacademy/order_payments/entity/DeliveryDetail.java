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

import com.nhnacademy.order_payments.model.DeliveryStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "DeliveryDetails")
public class DeliveryDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_detail_id")
    private Long id;

    @Setter
    @ManyToOne
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    @Setter
    @Column(name = "delivery_company_name")
    private String deliveryCompanyName;

    @Setter
    @Column(name = "delivery_man_name")
    private String deliveryManName;

    @Setter
    @Column(name = "estimated_at")
    private LocalDate estimatedAt; //배성 예정일이기에 일까지만

    @Setter
    @Column(name = "complete_at")
    private ZonedDateTime completeAt; //실제 도착한것이기에 시간까지

    @Setter
    @Column(name = "delivery_status")
    private DeliveryStatus deliveryStatus;

//    @Setter
//    @OneToMany(mappedBy = "deliveryDetail")
//    private List<OrderDetail> orderDetailList;

    @Setter
    @OneToMany(mappedBy = "deliveryDetail")
    private List<DeliveryOrderDetail> deliveryOrderDetails;
}
