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

import com.nhnacademy.order_payments.dto.order.UserInfoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "TEAM3-USER", path = "/api/internal/users")
public interface UserApiClient {

    /**
     * 1. userCreatedId를 경로에 담아 요청<p>
     * 2. UserInfoResponse 응답
     */
    // 주문서 작성 시 회원에게 받아올 정보
    @GetMapping("/{userCreatedId}/info")
    ResponseEntity<UserInfoResponse> getUserInfo(@PathVariable("userCreatedId") Long userCreatedId);

}

