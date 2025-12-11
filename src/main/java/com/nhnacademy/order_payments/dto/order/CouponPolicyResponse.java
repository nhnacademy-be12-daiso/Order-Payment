package com.nhnacademy.order_payments.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public final class CouponPolicyResponse {
    private Long couponPolicyId;
    private String couponPolicyName;
    private String couponType;
    private String discountWay;
    private BigDecimal discountAmount;
    private Long minOrderAmount;
    private Long maxDiscountAmount;
    private Integer validDays;
    private LocalDateTime validStartDate;
    private LocalDateTime validEndDate;
    private String policyStatus;
    private Integer quantity;
}