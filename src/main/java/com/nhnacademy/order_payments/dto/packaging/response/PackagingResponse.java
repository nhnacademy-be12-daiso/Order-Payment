package com.nhnacademy.order_payments.dto.packaging.response;

// 포장 정책 관련 데이터를 줄 DTO
public record PackagingResponse(
        Long packagingId,
        String packagingName,
        Long price,
        Boolean enabled
) {
}
