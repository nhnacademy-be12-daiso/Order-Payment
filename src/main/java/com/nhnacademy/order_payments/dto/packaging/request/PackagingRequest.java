package com.nhnacademy.order_payments.dto.packaging.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// 관리자 전용, 포장 정책 관련 데이터를 줄 DTO
public record PackagingRequest(
        @NotBlank
        String packagingName,

        @NotNull
        @Min(0)
        Long price,

        @NotNull
        Boolean enabled
) {
}
