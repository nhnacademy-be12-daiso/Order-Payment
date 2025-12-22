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

import com.nhnacademy.order_payments.dto.order.OrderCreateResponse;
import com.nhnacademy.order_payments.dto.order.OrderSummaryDto;
import com.nhnacademy.order_payments.entity.Order;
import com.nhnacademy.order_payments.exception.NotFoundOrderException;
import com.nhnacademy.order_payments.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    /**
     * ** 회원과 비회원의 주문 로직을 어떻게 분기할지? **
     * 회원 여부에 상관 없이 일단 해당 컨트롤러를 타고
     * 서비스 로직 안에서 분기
     */
    @PostMapping
    public ResponseEntity<OrderCreateResponse> order(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                                     @CookieValue(value = "X-Guest-Id", required = false)
                                                     String guestId,
                                                     @RequestBody OrderSummaryDto dto) {
        if (dto == null) {
            throw new NotFoundOrderException("주문 정보가 없습니다.");
        }

        Order order = orderService.precessOrderPayment(userId, guestId, dto);

        return ResponseEntity.ok(OrderCreateResponse.from(order));
    }
}
