package com.nhnacademy.order_payments.saga.common;

public enum ServiceStatus {
    PENDING,
    SUCCESS,
    FAILED,
    COMPENSATING, // 보상 진행 중
    COMPENSATED,
}
