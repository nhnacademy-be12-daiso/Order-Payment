package com.nhnacademy.order_payments.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Entity
@Table(name = "DeliveryPolicies")
@NoArgsConstructor
@Getter
public class DeliveryPolicy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_policy_id")
    private Long deliveryPolicyId;
    @Column(name = "delivery_policy_name")
    private String deliveryPolicyName;
    @Column(name = "delivery_fee")
    private Integer deliveryFee;
}
