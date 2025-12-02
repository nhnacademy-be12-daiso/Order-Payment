package com.nhnacademy.order_payments.dto;

public record BookApiResponse (
        long bookId,
        String title,
        int price
) {}
