package com.nhnacademy.order_payments.dto.order;

import java.util.List;

/**
 * FrontServer로 보내주는 종합 선물세트
 *
 */
public record PrepareOrderDto(
        BookInfoResponse bookInfoResponses,
        UserInfoResponse userInfoResponse,
        CouponResponse couponResponse
        // 추가될듯
) {}
