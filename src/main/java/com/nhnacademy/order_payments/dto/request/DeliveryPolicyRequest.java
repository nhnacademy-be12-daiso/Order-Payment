package com.nhnacademy.order_payments.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DeliveryPolicyRequest(
        @NotBlank
        @Schema(description = "정책명", example = "기본 배송비 정책")
        String policyName,

        @NotNull
        @Min(0)
        @Schema(description = "배송비", example = "3000")
        Long deliveryFee,

        @NotNull
        @Min(0)
        @Schema(description = "무료배송 최소 주문금액", example = "50000")
        Long freeMinimumAmount
) {
} // 관리자 전용, 배송 정책 관련 데이터를 줄 DTO
