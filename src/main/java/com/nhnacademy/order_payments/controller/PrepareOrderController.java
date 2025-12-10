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

package com.nhnacademy.order_payments.controller;

import com.nhnacademy.order_payments.dto.order.PrepareOrderDto;
import com.nhnacademy.order_payments.dto.request.order.PrepareOrderRequest;
import com.nhnacademy.order_payments.service.order.PrepareOrderService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 주문서 작성에 관련된 요청을 처리함
 */
@RestController
@RequestMapping("/api/orders/prepare")
@RequiredArgsConstructor
public class PrepareOrderController {

    private final PrepareOrderService prepareOrderService;

    @PostMapping
    ResponseEntity<PrepareOrderDto> getOrderPrepare(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                                    @RequestBody List<PrepareOrderRequest> requestList) {
        if (userId == null) { // 비회원의 경우 userId가 null로 넘어옴
            PrepareOrderDto dto = prepareOrderService.prepareGuestOrderInfo(requestList);
            return ResponseEntity.ok().body(dto);
        } else { // 회원인 경우
            PrepareOrderDto dto = prepareOrderService.prepareOrderInfo(userId, requestList);
            return ResponseEntity.ok().body(dto);
        }
    }

}
