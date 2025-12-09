package com.nhnacademy.order_payments.dto.cart;

public record BookApiResponse (
        long bookId,
        String title,
        int price
) {}
