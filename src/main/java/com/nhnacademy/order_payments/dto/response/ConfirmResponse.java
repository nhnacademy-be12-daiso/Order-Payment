package com.nhnacademy.order_payments.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

public record ConfirmResponse(
        @Schema(description = "주문 식별자", example = "1001")
        String orderId,
        @Schema(description = "결제 상태", example = "PAID")
        String status,
        @Schema(description = "승인 시각")
        OffsetDateTime approvedAt,
        @Schema(description = "결제 수단", example = "CARD")
        String method
) {}

