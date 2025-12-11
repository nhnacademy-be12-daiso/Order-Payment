package com.nhnacademy.order_payments.model;

public enum CouponStatus {
    ISSUED,     // 발급됨
    USED,       // 사용됨
    EXPIRED,    // 만료됨
    CANCELED    // 취소됨 (재사용 가능)
}
