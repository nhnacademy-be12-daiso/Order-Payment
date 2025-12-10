package com.nhnacademy.order_payments.service.order;

import com.nhnacademy.order_payments.event.OrderConfirmedEvent;
import com.nhnacademy.order_payments.event.OrderEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderEventPublisher eventPublisher;

    public void precessOrderPayment() {

        // TODO 주문 검증 및 Order DB에 임시 주문 정보 저장
        // ---> saga와는 무관한 로컬 트랜잭션임

        OrderConfirmedEvent event = null;

        eventPublisher.publishOrderConfirmedEvent(event);

    }

}
