package com.nhnacademy.order_payments.dto.order;


import com.nhnacademy.order_payments.entity.DeliveryPolicy;

// 필드 명 이렇게 대충해도 되나?
public record DeliveryPolicyDto (
    String name,
    Integer fee
) {
    public DeliveryPolicyDto(DeliveryPolicy deliveryPolicy) {
        this(deliveryPolicy.getDeliveryPolicyName(), deliveryPolicy.getDeliveryFee());
    }
}