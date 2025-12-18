package com.nhnacademy.order_payments.controller;

import com.nhnacademy.order_payments.dto.order.OrderCreateResponse;
import com.nhnacademy.order_payments.dto.order.OrderSummaryDto;
import com.nhnacademy.order_payments.entity.Order;
import com.nhnacademy.order_payments.exception.NotFoundOrderException;
import com.nhnacademy.order_payments.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/guest/orders")
@RequiredArgsConstructor
public class GuestOrderController {

    private final OrderService orderService;

    public ResponseEntity<OrderCreateResponse> order(@RequestBody OrderSummaryDto dto) {
        if (dto == null) {
            throw new NotFoundOrderException("주문 정보가 없습니다.");
        }

        Order order = orderService.precessOrderPayment(null, dto);
        return ResponseEntity.ok(OrderCreateResponse.from(order));
    }
}
