package com.nhnacademy.order_payments.dto.order;

public record DeliverySummaryDto(
        String receiverName, // 배송받을 사람 이름
        String deliveryAddress, // 도로명 주소
        String deliveryAddressDetail, // 상세 주소
        String postalCode, // 우편 번호
        String receiverPhoneNumber,
        Long DeliveryFee // 책정된 배송비
) {}
