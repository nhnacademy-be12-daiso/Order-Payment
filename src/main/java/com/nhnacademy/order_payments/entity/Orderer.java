package com.nhnacademy.order_payments.entity;

import com.nhnacademy.order_payments.model.Grade;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Orderers")
public class Orderer {

    @Id
    private Long ordererId;

    private Long userCreatedId; // 로그인 시, 회원한테서 받아옴

    private String phoneNumber;
    private String email;
    private Grade grade; // Enum으로 독립
}