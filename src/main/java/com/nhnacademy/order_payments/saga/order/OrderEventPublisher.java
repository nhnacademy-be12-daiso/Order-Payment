package com.nhnacademy.order_payments.saga.order;

import com.nhnacademy.order_payments.exception.ExternalServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderEventPublisher {

    /**
     * 실제 RabbitMQ로 메세지를 전송하는 로직
     */

    // --> RabbitMQ 통신을 위한 컴포넌트 주입
    private final AmqpTemplate rabbitTemplate;

    public void publishOrderOutboxMessage(String topic, String routingKey, String payload) {

        try {
            byte[] body = payload.getBytes(StandardCharsets.UTF_8);

            MessageProperties properties = new MessageProperties();
            properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            properties.setContentEncoding("UTF-8");
            Message message = new Message(body);

            rabbitTemplate.send(topic, routingKey, message); // 직렬화 해서 생으로 보냄

            log.info("[Orchestrator] ===== 메세지 발송됨 =====");
            log.info("[Orchestrator] Routing Key : {}", routingKey);

        } catch(Exception e) {
            log.warn("[Orchestrator] RabbitMQ 발행 실패 : {}", e.getMessage());
            throw new ExternalServiceException("rabbitMQ 메세지 발행 실패");
        }
    }


}
