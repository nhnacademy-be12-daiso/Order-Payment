package com.nhnacademy.order_payments.saga.payment;

import com.nhnacademy.order_payments.exception.ExternalServiceException;
import com.nhnacademy.order_payments.saga.common.OrderConfirmedEvent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.nio.charset.StandardCharsets;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentEventPublisher {
    private final AmqpTemplate rabbitTemplate;

    private static final String PAYMENT_EXCHANGE = "team3.saga.payment.exchange";
//    private static final String ROUTING_KEY_SUCCESS = "payment.success";
    @Value("${rabbitmq.routing.success}")
    private String ROUTING_KEY_COMPLETE;

//    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
//    public void publishBookDeductedEvent(OrderConfirmedEvent event) {
//        try {
//            rabbitTemplate.convertAndSend(
//                    PAYMENT_EXCHANGE,
//                    ROUTING_KEY_COMPLETE,
//                    event
//            );
//            log.info("[Payment API] 결제 성공 이벤트 발행 완료 : {}",ROUTING_KEY_COMPLETE);
//        } catch (Exception e) {
//            log.warn("[Payment API] RabbitMQ 발행 실패 : {}", e.getMessage());
//            // TODO : Outbox 패턴 또는 재시도 로직 구현해야함!!!
//        }
//    }

    public void publishPaymentOutboxMessage(String topic, String routingKey, String payload) {

        try {
            byte[] body = payload.getBytes(StandardCharsets.UTF_8);

            MessageProperties properties = new MessageProperties();
            properties.setContentType(MessageProperties.CONTENT_TYPE_JSON); // 👈 핵심 수정
            properties.setContentEncoding("UTF-8");
            Message message = new Message(body);

            rabbitTemplate.send(topic, routingKey, message); // 직렬화 해서 생으로 보냄

            log.info("[Payment API] 다음 이벤트 발행 완료 : Payment API -> Order API");
        } catch (Exception e) {
            log.warn("[Payment API] RabbitMQ 발행 실패 : {}", e.getMessage());
            throw new ExternalServiceException("rabbitMQ 메세지 발행 실패");
        }
    }
}
