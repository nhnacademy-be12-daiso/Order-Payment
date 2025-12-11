package com.nhnacademy.order_payments.dto.order;

import com.nhnacademy.order_payments.dto.response.DeliveryPolicyResponse;

import java.util.List;

/**
 * FrontServer로 보내주는 종합 선물세트
 *
 */
public record PrepareOrderDto(
        BookInfoResponse bookInfoResponses,
        UserInfoResponse userInfoResponse,
        CouponResponse couponResponse,
        // 추가될듯
        DeliveryPolicyResponse deliveryPolicy
) {}
