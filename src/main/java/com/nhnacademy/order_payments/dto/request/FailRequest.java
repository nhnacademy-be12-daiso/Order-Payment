package com.nhnacademy.order_payments.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FailRequest (
        @NotBlank String orderId,     // 주문번호
        @NotNull Long amount,      // 결제 시도 금액
        @NotBlank String paymentKey,
        @NotBlank String errorCode,
        @NotBlank String errorMessage
) {}