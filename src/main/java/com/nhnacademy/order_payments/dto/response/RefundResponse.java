package com.nhnacademy.order_payments.dto.response;

public record RefundResponse(
        String orderId,
        String status,
        String refundedAt,
        String method
) {}