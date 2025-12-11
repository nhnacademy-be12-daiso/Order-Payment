package com.nhnacademy.order_payments.dto.order;

import java.math.BigDecimal;

/**
 * 책 별로 적용되는 애들을 묶음
 * 1. 책
 * 2. 책 자체의 정가 + 할인가 + 할인률
 * 3. 적용된 쿠폰
 */

public record BookSummaryDto(
        Long bookId,
        Long couponId, // 먹인 쿠폰
        Long price, // 책 정가
        BigDecimal discountPercentage, // 할인률
        Long discountPrice, // 할인 금 액
        Integer quantity,
        Long packagingId// 주문자가 고른 포장지
) {}
