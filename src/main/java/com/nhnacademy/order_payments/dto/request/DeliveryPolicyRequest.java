package com.nhnacademy.order_payments.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DeliveryPolicyRequest(
        @NotBlank String policyName,
        @NotNull @Min(0) Integer deliveryFee,
        @NotNull @Min(0) Integer freeMinimumAmount
) {} // 관리자 전용, 배송 정책 관련 데이터를 줄 DTO
