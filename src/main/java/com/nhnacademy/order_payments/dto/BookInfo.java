package com.nhnacademy.order_payments.dto;

public record BookInfo(
        Long bookId,
        String title,
        int price,
        int quantity
) {
}
