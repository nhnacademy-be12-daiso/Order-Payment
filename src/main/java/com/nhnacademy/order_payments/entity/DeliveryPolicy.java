package com.nhnacademy.order_payments.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "DeliveryPolicies")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class DeliveryPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_policy_id")
    private Long deliveryPolicyId;

    @Column(name = "delivery_policy_name", nullable = false, length = 30)
    private String deliveryPolicyName;

    // 기본 배송비 = 5000원
    @Column(name = "delivery_fee", nullable = false)
    private Integer deliveryFee;

    // (회원)무료 배송 기준 주문 금액 = 30000원
    @Column(name = "free_minimum_amount", nullable = false)
    private Integer freeMinimumAmount;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public DeliveryPolicy(String deliveryPolicyName, Integer deliveryFee, Integer freeMinimumAmount) {
        this.deliveryPolicyName = deliveryPolicyName;
        this.deliveryFee = deliveryFee;
        this.freeMinimumAmount = freeMinimumAmount;
    }

    public void modifyPolicy(String deliveryPolicyName, Integer deliveryFee, Integer freeMinimumAmount) {
        this.deliveryPolicyName = deliveryPolicyName;
        this.deliveryFee = deliveryFee;
        this.freeMinimumAmount = freeMinimumAmount;
    }

}
