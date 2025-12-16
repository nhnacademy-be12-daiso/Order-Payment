package com.nhnacademy.order_payments.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RefundRequest (
        @NotBlank String orderId,
        String paymentKey, // 프론트에서 paymentKey를 넘겨주지 않고 저장된 데이터 불러오기
        String reason,
        Long cancelAmount
) {}