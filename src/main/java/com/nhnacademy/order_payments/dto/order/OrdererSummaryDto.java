package com.nhnacademy.order_payments.dto.order;

public record OrdererSummaryDto(
    String ordererName, // 주문자 이름
    String ordererPhoneNumber, // 주문자 전화번호
    String ordererEmail // 주문자 이메일
) {}
