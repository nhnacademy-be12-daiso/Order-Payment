package com.nhnacademy.order_payments.dto;

public record SyncInfo(
        Long userId,
        Long bookId,
        int quantity
) {}
