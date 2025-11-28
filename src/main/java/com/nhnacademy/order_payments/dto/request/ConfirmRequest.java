package com.nhnacademy.order_payments.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record ConfirmRequest(
        @Schema(description = "PG 제공자", example = "Fake or Toss")
        String provider,
        @Schema(description = "주문 식별자", example = "1001")
        String orderId,
        @Schema(description = "PG가 발급한 결제 키", example = "fake_key_001")
        String paymentKey,
        @Schema(description = "승인 금액(원)", example = "50000")
        Integer amount
) {}



