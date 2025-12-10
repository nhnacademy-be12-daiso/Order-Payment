package com.nhnacademy.order_payments.dto.cart;

public record BookItem(
        Long bookId,
        String title,
        Long price,
        int quantity
) {
}