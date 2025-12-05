package com.nhnacademy.order_payments.dto.response.order;

import com.nhnacademy.order_payments.model.Grade;
import com.nhnacademy.order_payments.model.OrderStatus;

import java.time.ZonedDateTime;

public record OrderResponse(
        long orderId,
        long orderNumber,
        OrderStatus orderStatus,
        ZonedDateTime orderDate,
        String ordererName,
        int totalPrice,
        String ordererPhoneNumber,
        String ordererEmail,
        Grade grade,

        DeliveryResponse delivery) {
}
