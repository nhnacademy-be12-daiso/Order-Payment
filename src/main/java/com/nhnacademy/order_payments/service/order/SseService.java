package com.nhnacademy.order_payments.service.order;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SseService {
    // 주문번호별로 연결(Emitter)을 저장하는 맵
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String orderId) {
        // 유효시간을 5분 정도로 설정 (결제/Saga 처리 시간 고려)
        SseEmitter emitter = new SseEmitter(5 * 60 * 1000L);

        emitters.put(orderId, emitter);

        // 연결이 종료되거나 타임아웃 시 맵에서 삭제
        emitter.onCompletion(() -> emitters.remove(orderId));
        emitter.onTimeout(() -> emitters.remove(orderId));

        // 첫 연결 시 깡통 데이터를 하나 보내야 연결이 유지됨
        try {
            emitter.send(SseEmitter.event().name("connect").data("connected!"));
        } catch (IOException e) {
            emitters.remove(orderId);
        }

        return emitter;
    }

    // Saga 로직이 끝나는 시점에 호출할 메서드
    public void notify(String orderId, String status) {
        SseEmitter emitter = emitters.get(orderId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("orderStatus").data(status));
            } catch (IOException e) {
                emitters.remove(orderId);
            }
        }
    }
}