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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "Deliveries")
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_id")
    private long id;

    @Setter
    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @Setter
    @Column(name = "deliver_address", nullable = false)
    private String address;

    @Setter
    @Column(name = "deliver_address_detail", nullable = false)
    private String addressDetail;

    @Setter
    @Column(name = "postal_code")
    private String postalCode;

    @Setter
    @Column(name = "receiver_name")
    private String receiverName;

    @Setter
    @Column(name = "receiver_phone_number")
    private String receiverPhoneNumber;

    @Column(name = "delivary_fee")  // 오타
    private int fee;

    public Delivery(String address, String postalCode, String receiverName, String receiverPhoneNumber, int fee) {
        this.address = address;
        this.postalCode = postalCode;
        this.receiverName = receiverName;
        this.receiverPhoneNumber = receiverPhoneNumber;
        this.fee = fee;
    }

    @Setter
    @OneToMany(mappedBy = "delivery")
    private List<DeliveryDetail> deliveryDetailList;
}
