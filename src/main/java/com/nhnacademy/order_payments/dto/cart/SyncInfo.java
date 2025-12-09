package com.nhnacademy.order_payments.dto.cart;

public record SyncInfo(
        Long userId,
        Long bookId,
        int quantity
) {}
