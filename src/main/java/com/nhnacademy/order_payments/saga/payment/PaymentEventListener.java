package com.nhnacademy.order_payments.saga.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.order_payments.entity.PaymentDeduplicationLog;
import com.nhnacademy.order_payments.entity.PaymentOutbox;
import com.nhnacademy.order_payments.exception.FailedSerializationException;
import com.nhnacademy.order_payments.exception.PaymentFailedException;
import com.nhnacademy.order_payments.repository.PaymentDeduplicationRepository;
import com.nhnacademy.order_payments.repository.PaymentOutboxRepository;
import com.nhnacademy.order_payments.saga.common.OrderCompensateEvent;
import com.nhnacademy.order_payments.saga.common.OrderConfirmedEvent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentEventListener {
    private final PaymentDeduplicationRepository deduplicationRepository;
    private final PaymentOutboxRepository paymentOutboxRepository;
    private final ApplicationEventPublisher publisher;
    private final PaymentEventPublisher paymentEventPublisher;
    private final ObjectMapper objectMapper;

    @Value("${rabbitmq.routing.success}")
    private String routingKey;

    @RabbitListener(queues = "${rabbitmq.queue.payment}")
    @Transactional
    public void handleOrderConfirmedEvent(OrderConfirmedEvent event) {

        log.info("[Payment API] ===== 주문 확정 이벤트 수신됨 =====");
        log.info("[Payment API] Order ID : {}", event.getOrderId());

        Long msgId = event.getOrderId();
        if(deduplicationRepository.existsById(msgId)) {
            log.warn("[Payment API] 중복 이벤트 수신 및 무시 : {}", msgId);
            return;
        }

        try {
            // TODO 실제 재고 차감 로직

            // 멱등성을 위한 로그 기록
            PaymentDeduplicationLog logEntry = new PaymentDeduplicationLog(msgId);
            deduplicationRepository.save(logEntry);

            try {
                PaymentOutbox outbox = new PaymentOutbox(
                        event.getOrderId(),
                        "PAYMENT",
                        "team3.saga.payment.exchange",
                        routingKey,
                        objectMapper.writeValueAsString(event)
                );
                paymentOutboxRepository.save(outbox);
                publisher.publishEvent(new PaymentOutboxCommittedEvent(this, outbox.getId()));
                // ----> 커밋 이벤트 발행

            } catch (JsonProcessingException e) {
                log.warn("객체 직렬화 실패");
                throw new FailedSerializationException("Failed to serialize event payload", e);
            }
            log.info("[Payment API] 결제 성공");
            log.info("[Payment API] 다음 이벤트 발행 완료 : Payment API -> Order API");

        } catch(PaymentFailedException e) { // 커스텀 예외 만들기!
//             TODO 재고 부족 혹은 실패 시 보상 트랜잭션 이벤트 발행
            log.error("[Payment API] 결제 실패 : {}", e.getMessage());
            log.error("[Payment API] ===== 결제 실패로 인한 보상 트랜잭션 시작 Order ID : {} =====", event.getOrderId());

            OrderCompensateEvent orderCompensateEvent = new OrderCompensateEvent(event, "PAYMENT_FAILED");

            try {
                paymentEventPublisher.publishPaymentOutboxMessage(
                        "Order용 익스체인지",
                        "Order용 라우팅 키",
                        objectMapper.writeValueAsString(orderCompensateEvent)
                );
            } catch (JsonProcessingException ex) {
                log.warn("객체 직렬화 실패");
                throw new FailedSerializationException("Failed to serialize event payload", ex);
            }

            PaymentDeduplicationLog logEntry = new PaymentDeduplicationLog(orderCompensateEvent.getOrderId());
            deduplicationRepository.save(logEntry);



            throw e;  // 트랜잭션 걸려있으므로 예외 던지면 DB 트랜잭션 롤백됨
        }
        catch(Exception e) {
            log.error("[Payment API] 이벤트 처리 중 예상치 못한 오류 발생 : {}", e.getMessage());
            // DLQ 처리
            throw new AmqpRejectAndDontRequeueException(e.getMessage(), e);
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.payment}")
    @Transactional
    public void handleOrderCompensateEvent(OrderCompensateEvent event) {

    }


}
