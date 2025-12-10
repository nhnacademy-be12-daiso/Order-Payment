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

package com.nhnacademy.order_payments.client;

import com.nhnacademy.order_payments.dto.order.CouponResponse;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "TEAM3-COUPON")
public interface CouponApiClient {

    /**
     * 1. userId(userCreatedId)를 경로에 담아 요청<p>
     * 2. CouponResponse 리스트 응답
     */
    // 주문서 작성 시 사용자가 사용할 수 있는 쿠폰 정보
    @GetMapping("/api/coupons/users/{userId}/available")
    ResponseEntity<List<CouponResponse>> getAvailableCoupons(@PathVariable Long userId);

}
