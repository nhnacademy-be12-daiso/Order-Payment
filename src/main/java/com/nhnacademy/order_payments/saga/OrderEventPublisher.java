package com.nhnacademy.order_payments.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderEventPublisher {

    // --> RabbitMQ 통신을 위한 컴포넌트 주입
    private final AmqpTemplate rabbitTemplate;

    private static final String ORDER_EXCHANGE = "team3.order.exchange";
    private static final String ROUTING_KEY_CONFIRMED = "order.confirmed";

    // saga 시작
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    // ----> OrderService의 트랜잭션이 커밋된 후에만 실행됨
    // >>>>>>> 이 부분 조금 더 공부해봐야 할듯 <<<<<<<<
    public void publishOrderConfirmedEvent(OrderConfirmedEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    ORDER_EXCHANGE,
                    ROUTING_KEY_CONFIRMED,
                    event
            );
            log.info("[Order API] Saga 시작 이벤트 발행 성공 : {}", ROUTING_KEY_CONFIRMED);

        } catch(Exception e) {
            log.warn("[Order API] RabbitMQ 발행 실패 : {}", e.getMessage());
            // TODO : Outbox 패턴 또는 재시도 로직 구현해야함!!!
        }
    }



}
