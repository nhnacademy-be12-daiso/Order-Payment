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

import com.nhnacademy.order_payments.dto.order.FinalPayloadDto;
import com.nhnacademy.order_payments.dto.order.OrderCreateResponse;
import com.nhnacademy.order_payments.dto.order.OrderSummaryDto;
import com.nhnacademy.order_payments.entity.Order;
import com.nhnacademy.order_payments.exception.NotFoundOrderException;
import com.nhnacademy.order_payments.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
                                              @RequestBody FinalPayloadDto payload) {

        OrderSummaryDto dto = payload.orderSummaryDto();
        Long orderId = Long.valueOf(payload.paymentConfirmRequest().getOrderId());

        if (dto == null) {
            throw new NotFoundOrderException("주문 정보가 없습니다.");
        }

        orderService.precessOrderPayment(userId, dto, orderId);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/create")
    public ResponseEntity<OrderCreateResponse> createOrder(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                                     @RequestBody OrderSummaryDto dto) {
        if (dto == null) {
            throw new NotFoundOrderException("주문 정보가 없습니다.");
        }

        // OrderID를 발급받아서 넘겨주는 역할만 함
        Order order = orderService.createOrder(userId, dto);

        return ResponseEntity.ok(OrderCreateResponse.from(order));
    }

    @GetMapping("/status/{orderId}")
    ResponseEntity<String> getOrderStatus(@PathVariable String orderId) {

    }
}
