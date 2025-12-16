package com.nhnacademy.order_payments.dto.order;


import com.nhnacademy.order_payments.entity.DeliveryPolicy;

// 필드 명 이렇게 대충해도 되나?
public record DeliveryPolicyDto (
    String name,
    Long fee
) {
    public DeliveryPolicyDto(DeliveryPolicy deliveryPolicy) {
        this(deliveryPolicy.getDeliveryPolicyName(), Long.valueOf(deliveryPolicy.getDeliveryFee()));
    }
}