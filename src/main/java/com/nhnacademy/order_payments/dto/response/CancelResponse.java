package com.nhnacademy.order_payments.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CancelResponse", description = "결제 취소/부분 취소 응답")
public record CancelResponse(
        @Schema(description = "주문 식별자")
        String orderId,
        @Schema(description = "상태", example = "CANCELED")
        String status,
        @Schema(description = "취소 시각(ISO-8601")
        String canceledAt,
        @Schema(description = "결제 수단", example = "TOSS")
        String method
) {
}
