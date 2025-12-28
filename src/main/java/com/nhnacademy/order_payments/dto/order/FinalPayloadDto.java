package com.nhnacademy.order_payments.dto.order;

public record FinalPayloadDto(
        PaymentConfirmRequest paymentConfirmRequest,
        OrderSummaryDto orderSummaryDto
) {
}