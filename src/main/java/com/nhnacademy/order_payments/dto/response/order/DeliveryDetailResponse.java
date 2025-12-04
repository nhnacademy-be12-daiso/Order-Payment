package com.nhnacademy.order_payments.dto.response.order;

import com.nhnacademy.order_payments.model.DeliveryStatus;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

public record DeliveryDetailResponse(
        Long deliveryDetailId,
        String DeliveryCompanyName,
        String deliveryManName,
        LocalDate estimatedAt,
        ZonedDateTime completeAt,
        DeliveryStatus deliveryStatus,
        List<OrderDetailResponse> orderDetailList) {
}
