package com.nhnacademy.order_payments.controller;

import com.nhnacademy.order_payments.dto.response.order.OrderListResponse;
import com.nhnacademy.order_payments.service.OrderResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/orders")
public class OderResultController {

    private final OrderResultService orderResultService;

    @GetMapping("/my")
    public OrderListResponse getMyOrders(@RequestHeader("X-User-Id") long userId){
        OrderListResponse orderList = orderResultService.getOrderList(userId);
        return orderList;
    }
}
