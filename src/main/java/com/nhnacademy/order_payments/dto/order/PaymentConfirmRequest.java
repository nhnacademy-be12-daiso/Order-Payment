package com.nhnacademy.order_payments.dto.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentConfirmRequest {
    private String provider;
    private String orderId;
    private String paymentKey;
    private Long amount;
}