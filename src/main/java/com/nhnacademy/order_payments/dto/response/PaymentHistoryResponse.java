package com.nhnacademy.order_payments.dto.response;

import com.nhnacademy.order_payments.model.PaymentEventType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record PaymentHistoryResponse(

        @Schema(description = "결제 타입", example = "APPROVE / CANCEL / PARTIAL_CANCEL / REFUND / FAIL")
        PaymentEventType eventType,

        @Schema(description = "금액", example = "50000")
        Long amount,

        @Schema(description = "사유", example = "단순 변심 / 품절 등")
        String reason,

        @Schema(description = "결제가 발생한 시각")
        LocalDateTime paymentTime,

        @Schema(description = "결제 수단", example = "CARD / EASY_PAY 등")
        String method
) {}
