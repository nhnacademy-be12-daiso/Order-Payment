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
public class PaymentEventPublisher {
    private final AmqpTemplate rabbitTemplate;

    private static final String PAYMENT_EXCHANGE = "team3.payment.exchange";
    private static final String ROUTING_KEY_SUCCESS = "payment.success";

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishBookDeductedEvent(OrderConfirmedEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    PAYMENT_EXCHANGE,
                    ROUTING_KEY_SUCCESS,
                    event
            );
            log.info("[Payment API] 결제 성공 이벤트 발행 완료 : {}", ROUTING_KEY_SUCCESS);
        } catch (Exception e) {
            log.warn("[Payment API] RabbitMQ 발행 실패 : {}", e.getMessage());
            // TODO : Outbox 패턴 또는 재시도 로직 구현해야함!!!
        }
    }
}
