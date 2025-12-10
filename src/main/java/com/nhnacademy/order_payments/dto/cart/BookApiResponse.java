package com.nhnacademy.order_payments.dto.cart;

public record BookApiResponse (
        Long bookId,
        String title,
        Long price
) {}
