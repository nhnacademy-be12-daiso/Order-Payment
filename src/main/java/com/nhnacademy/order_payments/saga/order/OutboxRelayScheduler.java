package com.nhnacademy.order_payments.saga.order;

import com.nhnacademy.order_payments.entity.OrderOutbox;
import com.nhnacademy.order_payments.model.OutboxStatus;
import com.nhnacademy.order_payments.repository.OrderOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * PENDING 상태로 남아있는 Outbox 메시지를 주기적으로 재발행하는 스케줄러.
 * OrderOutboxRelayProcessor가 즉시 발행에 실패했을 때 누락을 방지한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxRelayScheduler {

    private final OrderOutboxRepository orderOutboxRepository;
    private final OrderOutboxRelayProcessor orderOutboxRelayProcessor;

    @Scheduled(fixedDelay = 30_000) // 30초마다 실행
    public void relayPendingOutboxes() {
        List<OrderOutbox> pendingList = orderOutboxRepository.findByStatus(OutboxStatus.PENDING);

        if (pendingList.isEmpty()) {
            return;
        }

        log.info("[OutboxScheduler] PENDING 메시지 {}건 재발행 시도", pendingList.size());

        for (OrderOutbox outbox : pendingList) {
            try {
                orderOutboxRelayProcessor.processRelay(outbox.getId());
            } catch (Exception e) {
                log.warn("[OutboxScheduler] OutboxID {} 처리 중 오류 발생: {}", outbox.getId(), e.getMessage());
            }
        }
    }
}
