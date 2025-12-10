package com.nhnacademy.order_payments.dto.response.order;

import com.nhnacademy.order_payments.model.Grade;
import com.nhnacademy.order_payments.model.OrderStatus;

import java.time.ZonedDateTime;

public record OrderResponse(
        Long orderId,
        Long orderNumber,
        OrderStatus orderStatus,
        ZonedDateTime orderDate,
        String ordererName,
        Long totalPrice,
        String ordererPhoneNumber,
        String ordererEmail,
        Grade grade,

        DeliveryResponse delivery) {
}
