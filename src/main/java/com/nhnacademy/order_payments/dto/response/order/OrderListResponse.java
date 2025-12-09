package com.nhnacademy.order_payments.dto.response.order;

import java.util.List;

public record OrderListResponse(
        List<OrderResponse> orderList) {
}
