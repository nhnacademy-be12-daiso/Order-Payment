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

import com.nhnacademy.order_payments.dto.response.order.OrderListResponse;
import com.nhnacademy.order_payments.service.order.OrderResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/orders")
public class OrderResultController {

    private final OrderResultService orderResultService;

    @GetMapping("/my")
    public OrderListResponse getMyOrders(@RequestHeader("X-User-Id") Long userId) {
        // 주문 내역은 회원만 조회 가능
        // 주문서 작성시 썼던 주문자 정보(이름, 연락처, 이메일 등)랑 주문 번호(orderNumber) 받아서 처리하는 비회원 전용 컨트롤러 만들어야 될 듯
        return orderResultService.getOrderList(userId, null);
    }
}
