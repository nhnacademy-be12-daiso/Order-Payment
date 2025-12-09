package com.nhnacademy.order_payments.dto.order;

import com.nhnacademy.order_payments.entity.DeliveryPolicy;

import java.util.List;

/**
 * FrontServer로 보내주는 종합 선물세트
 *
 */
public record PrepareOrderDto(
        BookInfoResponse bookInfoResponses,
        UserInfoResponse userInfoResponse,
        List<CouponResponse> couponResponseList,
        List<PackagingDto> packagingList,
        DeliveryPolicyDto deliveryPolicyDto
) {
    public PrepareOrderDto(
            BookInfoResponse bookInfoResponses,
            List<PackagingDto> packagingList,
            DeliveryPolicyDto deliveryPolicyDto) {

        this(
                bookInfoResponses,
                null, // 비회원이므로 userInfoResponse는 null
                null, // 비회원이므로 couponResponse는 null
                packagingList,
                deliveryPolicyDto
        );
    }
}
