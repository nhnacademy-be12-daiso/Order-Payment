package com.nhnacademy.order_payments.dto.order;

import java.math.BigDecimal;

/**
 * 책 별로 적용되는 애들을 묶음
 * 1. 책
 * 2. 책 자체의 정가 + 할인가 + 할인률
 * 3. 적용된 쿠폰
 */

public record BookOrderUnit(
        Long bookId,
        Long couponId,
        Long price,
        BigDecimal discountPercentage,
        Long discountPrice,

) {}
