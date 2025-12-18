package com.nhnacademy.order_payments.saga.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 이 계층이 왜 필요한가?
 * --> 자가 호출(Self-invocation) 문제를 해결하기 위해서임
 * --> 트랜잭션을 걸지 않음 --> 순수한 이벤트 핸들러 역할
 */

@Service
@RequiredArgsConstructor
public class OrderOutboxRelayManager {
    private final OrderOutboxRelayProcessor orderOutboxRelayProcessor;

    // OrderOutbox 엔티티가 DB에 커밋된 후에만 실행됨
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) // 트랜잭션이 커밋된 이후 실행됨
    public void handleOutboxCommitted(OrderOutboxCommittedEvent event) {
        // DB 커밋 성공 후, Relay 서비스를 호출하여 방금 저장된 Outbox 메시지를 처리하도록 알림
        orderOutboxRelayProcessor.processRelay(event.getOutboxId());
    }
}
