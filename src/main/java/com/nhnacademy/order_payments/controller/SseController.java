package com.nhnacademy.order_payments.controller;

import com.nhnacademy.order_payments.service.order.SseService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class SseController {
    private final SseService sseService;

    public SseController(SseService sseService) {
        this.sseService = sseService;
    }

    @CrossOrigin(origins = "*") // 테스트를 위해 우선 모두 허용
    @GetMapping(value = "/api/subscribe/{orderId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@PathVariable String orderId) {
        return sseService.subscribe(orderId);
    }
}