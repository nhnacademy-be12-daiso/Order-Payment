package com.nhnacademy.order_payments.model;

public enum PaymentEventType {
    APPROVE, //승인
    CANCEL, //취소
    PARTIAL_CANCEL, //부분 취소
    REFUND, //환불
    FAIL //실패
}
