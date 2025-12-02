package com.nhnacademy.order_payments.dto;

public record BookItem(
        Long bookId,
        String title,
        int price,
        int quantity
) {
}