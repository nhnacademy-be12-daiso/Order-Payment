package com.nhnacademy.order_payments.dto.packaging.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// 관리자 전용, 포장 정책 관련 데이터를 줄 DTO
public record PackagingRequest(
        @NotBlank
        @Schema(description = "포장명", example = "기본 포장")
        String packagingName,

        @NotNull
        @Min(0)
        @Schema(description = "포장 가격", example = "1000")
        Long price,

        @NotNull
        @Schema(description = "활성화 여부", example = "true")
        Boolean enabled
) {
}
