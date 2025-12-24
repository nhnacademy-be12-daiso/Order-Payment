package com.nhnacademy.order_payments.saga.todelete;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.order_payments.entity.PaymentDeduplicationLog;
import com.nhnacademy.order_payments.entity.PaymentOutbox;
import com.nhnacademy.order_payments.exception.FailedSerializationException;
import com.nhnacademy.order_payments.exception.PaymentFailedException;
import com.nhnacademy.order_payments.repository.PaymentDeduplicationRepository;
import com.nhnacademy.order_payments.repository.PaymentOutboxRepository;
import com.nhnacademy.order_payments.saga.event.OrderCompensateEvent;
import com.nhnacademy.order_payments.saga.event.OrderConfirmedEvent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentEventListener {
    private final PaymentDeduplicationRepository deduplicationRepository;
    private final PaymentOutboxRepository paymentOutboxRepository;
    private final ApplicationEventPublisher publisher;
    private final ObjectMapper objectMapper;
    private final CompensationOutboxService compensationOutboxService;
//
//    @RabbitListener(bindings = @QueueBinding(
//            value = @Queue(
//                    value = "#{@Saga.COUPON_SUCCESS.getQueue()}",
//                    durable = "true"
//            ),
//            exchange = @Exchange(
//                    value = "#{@Saga.COUPON_SUCCESS.getExchange()}"
//            ),
//            key = "#{@Saga.COUPON_SUCCESS.getRoutingKey()}"
//    ))
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
            // TODO 실제 주문 로직

            // 멱등성을 위한 로그 기록
            PaymentDeduplicationLog logEntry = new PaymentDeduplicationLog(msgId.toString());
            deduplicationRepository.save(logEntry);

            // 정합성을 위한 Outbox
            try {
                PaymentOutbox outbox = new PaymentOutbox(
                        event.getOrderId(),
                        "PAYMENT",
                        "",
                        "",
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

        } catch(PaymentFailedException e) {
//             TODO 재고 부족 혹은 실패 시 보상 트랜잭션 이벤트 발행
            log.error("[Payment API] 결제 실패 : {}", e.getMessage());
            log.error("[Payment API] ===== 결제 실패로 인한 보상 트랜잭션 시작 Order ID : {} =====", event.getOrderId());

            OrderCompensateEvent orderCompensateEvent = new OrderCompensateEvent(event, "PAYMENT_FAILED");

            compensationOutboxService.saveCompensationEvent(
                    event.getOrderId(),
                    orderCompensateEvent,
                    SagaTopic2.PAYMENT_COMPENSATION
            );

            // Order로 직접 쏴줌
            compensationOutboxService.saveCompensationEvent(
                    event.getOrderId(),
                    orderCompensateEvent,
                    SagaTopic2.PAYMENT_NOTIFICATION
            );

            throw e;  // Payment 서비스 로직 롤백됨 (보상 이벤트는 남아있음!)
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
