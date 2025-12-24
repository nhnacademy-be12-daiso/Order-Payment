package com.nhnacademy.order_payments.saga.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.order_payments.entity.PaymentDeduplicationLog;
import com.nhnacademy.order_payments.entity.PaymentOutbox;
import com.nhnacademy.order_payments.exception.FailedSerializationException;
import com.nhnacademy.order_payments.repository.PaymentDeduplicationRepository;
import com.nhnacademy.order_payments.repository.PaymentOutboxRepository;
import com.nhnacademy.order_payments.saga.common.SagaTopic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompensationOutboxService {
    private final PaymentOutboxRepository paymentOutboxRepository;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher publisher;
    private final PaymentDeduplicationRepository paymentDeduplicationRepository;

    // 기존 트랜잭션과 상관 없는 새 트랜잭션을 시작함
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveCompensationEvent(Long orderId, Object event, SagaTopic topic) {

        String dedupKey = orderId + "_PAYMENT_FAIL";
        if(paymentDeduplicationRepository.existsByMessageId(dedupKey)) {
            log.warn("[Compensation] 중복 이벤트 수신 및 무시 : {}", orderId);
            return;
        }

        try {

            // 멱등성 로그 기록
            PaymentDeduplicationLog logEntry = new PaymentDeduplicationLog(dedupKey);
            paymentDeduplicationRepository.save(logEntry);

            PaymentOutbox outbox = new PaymentOutbox(
                    orderId,
                    "PAYMENT",
                    topic.getExchange(),
                    topic.getRoutingKey(),
                    objectMapper.writeValueAsString(event)
            );
            // 이벤트 발행
            paymentOutboxRepository.save(outbox);
            publisher.publishEvent(new PaymentOutboxCommittedEvent(this, outbox.getId()));
            // ---> 얘가 알아서 쏴줌

        } catch (JsonProcessingException ex) {
            log.warn("객체 직렬화 실패");
            throw new FailedSerializationException("Failed to serialize event payload", ex);
        }
    }



}

