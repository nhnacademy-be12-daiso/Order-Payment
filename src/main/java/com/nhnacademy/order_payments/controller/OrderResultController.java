package com.nhnacademy.order_payments.controller;

import com.nhnacademy.order_payments.service.order.OrderResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/orders")
public class OrderResultController {

    private final OrderResultService orderResultService;

    @GetMapping("/my")
    public List<Object> getMyOrders(@RequestHeader("X-User-Id") long userId){
        return orderResultService.getOderList(userId);
    }
}
