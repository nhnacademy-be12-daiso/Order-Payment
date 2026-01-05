package com.nhnacademy.order_payments.dto.order;

public record OrderCancelRequest (
        long bookId,
        int quantity
) {
}
