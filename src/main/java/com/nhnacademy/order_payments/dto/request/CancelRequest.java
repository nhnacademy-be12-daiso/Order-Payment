package com.nhnacademy.order_payments.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CancelRequest", description = "결제 취소/부분 취소 요청")
public record CancelRequest(
        @Schema(description = "주문 식별자", example = "1001")
        String orderId,
        @Schema(description = "PG가 발급한 결제 키", example = "fake_key_001")
        String paymentKey,
        @Schema(description = "취소 사유", example = "고객 단순 변심")
        String reason,
        @Schema(description = "환불 금액, null이면 전액 취소", example = "50000")
        Integer cancelAmount
) {}
