package com.nhnacademy.order_payments.dto.packaging.response;

import io.swagger.v3.oas.annotations.media.Schema;

// 포장 정책 관련 데이터를 줄 DTO
public record PackagingResponse(
        @Schema(description = "포장 정책 ID", example = "1")
        Long packagingId,

        @Schema(description = "포장명", example = "기본 포장")
        String packagingName,

        @Schema(description = "포장 가격", example = "1000")
        Long price,

        @Schema(description = "활성화 여부", example = "true")
        Boolean enabled
) {
}
