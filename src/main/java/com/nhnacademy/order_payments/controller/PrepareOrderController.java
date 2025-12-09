package com.nhnacademy.order_payments.controller;

import com.nhnacademy.order_payments.dto.order.PrepareOrderDto;
import com.nhnacademy.order_payments.service.order.PrepareOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 주문서 작성에 관련된 요청을 처리함
 */
@RestController
@RequestMapping("/api/orders/prepare")
@RequiredArgsConstructor
public class PrepareOrderController {

    private final PrepareOrderService prepareOrderService;

    @PostMapping
    ResponseEntity<PrepareOrderDto> getOrderPrepare(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                                    @RequestBody List<Long> bookIdList) {
        if(userId == null) { // 비회원의 경우 userId가 null로 넘어옴
            PrepareOrderDto dto = prepareOrderService.prepareGuestOrderInfo(bookIdList);
            return ResponseEntity.ok().body(dto);
        }
        else { // 회원인 경우
            PrepareOrderDto dto = prepareOrderService.prepareOrderInfo(userId, bookIdList);
            return ResponseEntity.ok().body(dto);
        }
    }
}
