package com.nhnacademy.order_payments.dto.order;

/**
 * 주문서 작성 시
 * user에게서 받아올 정보
 * 1. 사용가능한 포인트
 * 2. 현재 회원의 등급
 */

public record UserInfoResponse(
        Long userId,
        int point,
        String gradeName // 등급명?
) {}
