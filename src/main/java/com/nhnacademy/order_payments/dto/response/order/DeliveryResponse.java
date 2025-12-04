package com.nhnacademy.order_payments.dto.response.order;

import java.util.List;

public record DeliveryResponse (
        long deliveryId,
        String deliverAddress,
        String deliverAddressDetail,
        String postalCode,
        String receiverName,
        String ReceiverPhoneNumber,
        int deliveryFee,
        List<DeliveryDetailResponse> deliveryDetailList) {
}
