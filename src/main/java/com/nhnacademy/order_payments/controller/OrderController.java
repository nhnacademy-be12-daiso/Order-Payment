package com.nhnacademy.order_payments.controller;

import com.nhnacademy.order_payments.dto.order.OrderSummaryDto;
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
     */

    @PostMapping
    public ResponseEntity<Void> order(@RequestHeader("X-User-Id") Long userId, @RequestBody OrderSummaryDto dto) {
        // TODO : 반환 DTO 정하기

        if(dto == null) {
            throw new NotFoundOrderException("주문 정보가 없습니다.");
        }

        orderService.precessOrderPayment(userId, dto);

        return ResponseEntity.ok().build();
    }


}
