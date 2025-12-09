package com.nhnacademy.order_payments.dto.order;


import com.nhnacademy.order_payments.model.CouponType;
import com.nhnacademy.order_payments.model.DiscountWay;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 주문서 작성 시
 * 사용자가 사용할 수 있는 쿠폰 정보
 */
// 쿠폰 기본 정보 응답
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CouponResponse {
    private Long userCouponId;
    private Long userId;
    private CouponPolicyResponse couponPolicy;  // 중첩 객체
    private String status;  // CouponStatus
    private LocalDateTime issuedAt;
    private LocalDateTime expiryAt;
    private LocalDateTime usedAt;
}
