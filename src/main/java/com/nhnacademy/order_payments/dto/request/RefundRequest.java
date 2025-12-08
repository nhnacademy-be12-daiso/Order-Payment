package com.nhnacademy.order_payments.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RefundRequest (
        @NotBlank String orderId,
        @NotBlank String paymentKey,
        String reason,
        @NotNull Integer cancelAmount
) {}