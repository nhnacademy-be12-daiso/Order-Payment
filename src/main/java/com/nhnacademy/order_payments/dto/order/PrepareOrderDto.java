/*
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 * + Copyright 2025. NHN Academy Corp. All rights reserved.
 * + * While every precaution has been taken in the preparation of this resource,  assumes no
 * + responsibility for errors or omissions, or for damages resulting from the use of the information
 * + contained herein
 * + No part of this resource may be reproduced, stored in a retrieval system, or transmitted, in any
 * + form or by any means, electronic, mechanical, photocopying, recording, or otherwise, without the
 * + prior written permission.
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 */

package com.nhnacademy.order_payments.dto.order;

import com.nhnacademy.order_payments.dto.response.DeliveryPolicyResponse;
import java.util.List;

/**
 * FrontServer로 보내주는 종합 선물세트
 *
 */
public record PrepareOrderDto(
        InternalBooksInfoResponse booksInfoResponse,
        UserInfoResponse userInfoResponse,
        List<CouponResponse> couponResponseList,
        List<PackagingDto> packagingList,
        DeliveryPolicyResponse deliveryPolicyResponse
) {
    public PrepareOrderDto(
            InternalBooksInfoResponse booksInfoResponse,
            List<PackagingDto> packagingList,
            DeliveryPolicyResponse deliveryPolicyResponse) {

        this(
                booksInfoResponse,
                null, // 비회원이므로 userInfoResponse는 null
                null, // 비회원이므로 couponResponse는 null
                packagingList,
                deliveryPolicyResponse
        );
    }
}
