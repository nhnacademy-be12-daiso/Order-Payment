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

import com.nhnacademy.order_payments.dto.order.GuestOrderCheckRequest;
import com.nhnacademy.order_payments.dto.response.order.DeliveryAddressResponse;
import com.nhnacademy.order_payments.dto.response.order.OrderListResponse;
import com.nhnacademy.order_payments.dto.response.order.OrderResponse;
import com.nhnacademy.order_payments.exception.IllegalReturnStateException;
import com.nhnacademy.order_payments.service.order.OrderResultService;
import jakarta.ws.rs.Path;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/orders")
public class OrderResultController {

    private final OrderResultService orderResultService;

    @GetMapping("/my")
    public OrderListResponse getMyOrders(@RequestHeader("X-User-Id") Long userId) {
        // 주문 내역은 회원만 조회 가능
        // 주문서 작성시 썼던 주문자 정보(이름, 연락처, 이메일 등)랑 주문 번호(orderNumber) 받아서 처리하는 비회원 전용 컨트롤러 만들어야 될 듯
        return orderResultService.getOrderList(userId);
    }

    @PostMapping("/guest")
    public OrderResponse getGuestOrder(@RequestBody GuestOrderCheckRequest request) {
        return orderResultService.getGuestOrder(request.orderNumber(), request.password());
    }

    @PostMapping("/{orderDetailId}/refund")
    ResponseEntity<?> refundOrder(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                  @PathVariable Long orderDetailId) {
        try {
            orderResultService.refundOrder(orderDetailId);
            return ResponseEntity.ok("반품 신청이 접수되었습니다.");
        } catch (IllegalReturnStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }


    }

    @PostMapping("/guest/delivery")
    public DeliveryAddressResponse getGuestDelivery(@RequestBody GuestOrderCheckRequest request) {
        return orderResultService.getGuestDelivery(request.orderNumber(), request.password());
    }

    @GetMapping("/{orderNumber}/delivery")
    public DeliveryAddressResponse getMemberDelivery(@RequestHeader("X-User-Id") Long userId,
                                                     @PathVariable Long orderNumber) {
        return orderResultService.getMemberDelivery(userId, orderNumber);
    }

}
