package com.nhnacademy.order_payments.saga.order;


import com.nhnacademy.order_payments.entity.OrderOutbox;
import com.nhnacademy.order_payments.exception.ExternalServiceException;
import com.nhnacademy.order_payments.repository.OrderOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderOutboxRelayProcessor {

    private final OrderEventPublisher orderEventPublisher;
    private final OrderOutboxRepository orderOutboxRepository;

    // 실제 통신 + DB에 저장하는 로직
    // 완전히 독립적인 트랜잭션을 위해 붙여줌
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processRelay(Long outboxId) {

        OrderOutbox outbox = orderOutboxRepository.findById(outboxId).orElseThrow();
        // ---> 방금 저장한 outbox 꺼내옴

        try {
            orderEventPublisher.publishOrderOutboxMessage(
                    outbox.getTopic(),
                    outbox.getRoutingKey(),
                    outbox.getPayload()
            );
            log.info("[Orchestrator] Order ID : {}", outbox.getAggregateId());
            outbox.markAsPublished();
            orderOutboxRepository.save(outbox);

        } catch(ExternalServiceException e) {
            // REQUIRES_NEW 트랜잭션 안에서 save() 후 throw 하지 않아야 재시도 카운터가 보존됨
            if(outbox.getRetryCount() < 3) {
                outbox.incrementRetryCount();
                orderOutboxRepository.save(outbox);
                log.warn("[Orchestrator] Outbox 발행 실패, 재시도 예약 OutboxID : {}, 시도 횟수: {}", outboxId, outbox.getRetryCount());
            } else {
                outbox.markAsFailed();
                orderOutboxRepository.save(outbox);
                log.error("[Orchestrator] Outbox 메세지 최종 발행 실패 OutboxID : {}", outboxId);
            }
        }
    }
}
