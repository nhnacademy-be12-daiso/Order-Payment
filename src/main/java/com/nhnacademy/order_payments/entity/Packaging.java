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
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "Packaging")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Packaging {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "packaging_id")
    private Long packagingId;

    @Column(name = "wrapping_paper_name", nullable = false)
    private String packagingName;

    @Column(name = "wrapping_paper_price", nullable = false)
    private Long price;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled; // 포장지 사용 가능 여부

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Packaging(String packagingName, Long price, Boolean enabled) {
        this.packagingName = packagingName;
        this.price = price;
        this.enabled = enabled;
    }

    // 포장 정책 수정용 도메인 메서드
    public void modifyPackaging(String packagingName, Long price, Boolean enabled) {
        this.packagingName = packagingName;
        this.price = price;
        this.enabled = enabled;
    }
}
