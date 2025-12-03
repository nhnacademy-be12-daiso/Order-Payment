package com.nhnacademy.order_payments.controller;

import com.nhnacademy.order_payments.service.OderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/orders")
public class OderController {

    private final OderService oderService;

    @GetMapping("/my")
    public List<Object> getMyOrders(@RequestHeader("X-User-Id") long userId){
        return oderService.getOderList(userId);
    }
}
