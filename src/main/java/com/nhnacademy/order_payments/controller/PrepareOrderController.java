package com.nhnacademy.order_payments.controller;

import com.nhnacademy.order_payments.dto.order.PrepareOrderDto;
import com.nhnacademy.order_payments.service.order.PrepareOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("/api/prepare") // -----> API 네이밍 좀 에바임
@RequiredArgsConstructor
public class PrepareOrderController {

    private final PrepareOrderService prepareOrderService;

    @GetMapping
    public ResponseEntity<PrepareOrderDto> prepareOrderData(Long userId, List<Long> bookIdList) {

        PrepareOrderDto dto = prepareOrderService.prepareOrderData(userId, bookIdList);

        return ResponseEntity.ok().body(dto);
    }

}
