package com.nhnacademy.order_payments.dto.response.order;

import java.time.LocalDate;

public record DeliveryAddressResponse(
        String deliveryAddress,
        String deliveryAddressDetail,
        String postalCode,
        LocalDate estimatedAt
) {
}
