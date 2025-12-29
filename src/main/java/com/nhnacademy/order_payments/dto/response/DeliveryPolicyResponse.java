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

package com.nhnacademy.order_payments.dto.response;

import com.nhnacademy.order_payments.entity.DeliveryPolicy;
import io.swagger.v3.oas.annotations.media.Schema;

public record DeliveryPolicyResponse(
        @Schema(description = "배송비 정책 ID", example = "1")
        Long deliveryPolicyId,

        @Schema(description = "정책명", example = "기본 배송비 정책")
        String policyName,

        @Schema(description = "배송비", example = "3000")
        Long deliveryFee,

        @Schema(description = "무료배송 최소 주문금액", example = "50000")
        Long freeMinimumAmount
) {
    // 배송 정책 관련 데이터를 응답할 DTO

    public DeliveryPolicyResponse(DeliveryPolicy deliveryPolicy) {
        this(
                deliveryPolicy.getDeliveryPolicyId(),
                deliveryPolicy.getDeliveryPolicyName(),
                deliveryPolicy.getDeliveryFee(),
                deliveryPolicy.getFreeMinimumAmount()
        );
    }
}
