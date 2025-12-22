package com.nhnacademy.order_payments.saga.payment;

import com.nhnacademy.order_payments.entity.PaymentOutbox;
import com.nhnacademy.order_payments.exception.ExternalServiceException;
import com.nhnacademy.order_payments.repository.PaymentOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentOutboxRelayProcessor {

    private final PaymentEventPublisher paymentEventPublisher;
    private final PaymentOutboxRepository paymentOutboxRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processRelay(Long outboxId) {

        PaymentOutbox outbox = paymentOutboxRepository.findById(outboxId).orElseThrow();
        // <<<<<< 예외처리

        try {
            paymentEventPublisher.publishPaymentOutboxMessage(
                    outbox.getTopic(),
                    outbox.getRoutingKey(),
                    outbox.getPayload()
            );

            log.info("[Payment API] Order ID : {}", outbox.getAggregateId());
            outbox.markAsPublished();
            paymentOutboxRepository.save(outbox);

        } catch(ExternalServiceException e) { // 실패시 재시도 및 롤백
            if (outbox.getRetryCount() < 3) {
                outbox.incrementRetryCount();
                paymentOutboxRepository.save(outbox); // DB에 업데이트
            } else {
                outbox.markAsFailed();
                paymentOutboxRepository.save(outbox); // DB에 업데이트
                log.error("[Payment API] Outbox 메세지 최종 발행 실패 OutboxID : {}", outboxId);
            }
            throw e; // 예외 던져서 롤백 유도
        }
    }
}
