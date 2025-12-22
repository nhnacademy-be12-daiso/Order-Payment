package com.nhnacademy.order_payments.dto.order;

import com.nhnacademy.order_payments.entity.Order;

// 주문서에서 결제하기 눌렀을떼 pg결제에 필요한 정보 반환
public record OrderCreateResponse(
        Long orderId,
        Long orderNumber,
        String orderIdForPayment,
        Long amount
) {
    public static OrderCreateResponse from(Order order) {
        return new OrderCreateResponse(
                order.getId(),
                order.getOrderNumber(),
                String.valueOf(order.getOrderNumber()),
                order.getTotalPrice()
        );
    }
}
