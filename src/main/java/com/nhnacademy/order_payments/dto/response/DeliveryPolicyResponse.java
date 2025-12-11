package com.nhnacademy.order_payments.dto.response;

public record DeliveryPolicyResponse(
        Long deliveryPolicyId,
        String policyName,
        Integer deliveryFee,
        Integer freeMinimumAmount
) {} // 배송 정책 관련 데이터를 응답할 DTO
