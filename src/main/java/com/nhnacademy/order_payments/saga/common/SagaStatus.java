package com.nhnacademy.order_payments.saga.common;

public enum SagaStatus {
    STARTED, // 사가 시작 전?
    PROCESSING, // 진행 중
    COMPLETED, // 사가 완료
    COMPENSATING, // 보상 메세지 보냄
    COMPENSATED  // 보상
}
