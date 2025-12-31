package com.nhnacademy.order_payments.dto.response.order;

public record DeliveryAddressResponse(
        String deliveryAddress,
        String deliveryAddressDetail,
        String postalCode
) {
}
