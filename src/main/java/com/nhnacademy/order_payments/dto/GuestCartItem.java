package com.nhnacademy.order_payments.dto;

public record GuestCartItem(
        Long bookId,
        String title,
        int price,
        int quantity
) {
}
