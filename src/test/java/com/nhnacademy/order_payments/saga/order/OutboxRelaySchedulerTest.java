package com.nhnacademy.order_payments.saga.order;

import com.nhnacademy.order_payments.entity.OrderOutbox;
import com.nhnacademy.order_payments.model.OutboxStatus;
import com.nhnacademy.order_payments.repository.OrderOutboxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxRelaySchedulerTest {

    @Mock
    OrderOutboxRepository orderOutboxRepository;

    @Mock
    OrderOutboxRelayProcessor orderOutboxRelayProcessor;

    OutboxRelayScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new OutboxRelayScheduler(orderOutboxRepository, orderOutboxRelayProcessor);
    }

    @Test
    @DisplayName("PENDING 메시지가 없으면 processRelay를 호출하지 않는다")
    void relayPendingOutboxes_noPending_doesNothing() {
        when(orderOutboxRepository.findByStatus(OutboxStatus.PENDING))
                .thenReturn(Collections.emptyList());

        scheduler.relayPendingOutboxes();

        verify(orderOutboxRelayProcessor, never()).processRelay(any());
    }

    @Test
    @DisplayName("PENDING 메시지가 있으면 각 outbox ID로 processRelay를 호출한다")
    void relayPendingOutboxes_withPending_callsProcessRelayForEach() {
        OrderOutbox outbox1 = pendingOutbox(10L);
        OrderOutbox outbox2 = pendingOutbox(20L);
        when(orderOutboxRepository.findByStatus(OutboxStatus.PENDING))
                .thenReturn(List.of(outbox1, outbox2));

        scheduler.relayPendingOutboxes();

        verify(orderOutboxRelayProcessor).processRelay(10L);
        verify(orderOutboxRelayProcessor).processRelay(20L);
    }

    @Test
    @DisplayName("processRelay에서 예외가 발생해도 나머지 항목을 계속 처리한다")
    void relayPendingOutboxes_oneThrows_continuesWithRest() {
        OrderOutbox outbox1 = pendingOutbox(30L);
        OrderOutbox outbox2 = pendingOutbox(40L);
        when(orderOutboxRepository.findByStatus(OutboxStatus.PENDING))
                .thenReturn(List.of(outbox1, outbox2));

        doThrow(new RuntimeException("RabbitMQ 연결 실패"))
                .when(orderOutboxRelayProcessor).processRelay(30L);

        scheduler.relayPendingOutboxes();

        verify(orderOutboxRelayProcessor).processRelay(30L);
        verify(orderOutboxRelayProcessor).processRelay(40L); // 예외 이후에도 계속
    }

    @Test
    @DisplayName("PENDING 메시지 3개 모두 정상 처리되면 processRelay가 정확히 3번 호출된다")
    void relayPendingOutboxes_threeItems_callsThreeTimes() {
        List<OrderOutbox> outboxes = List.of(
                pendingOutbox(1L), pendingOutbox(2L), pendingOutbox(3L)
        );
        when(orderOutboxRepository.findByStatus(OutboxStatus.PENDING)).thenReturn(outboxes);

        scheduler.relayPendingOutboxes();

        verify(orderOutboxRelayProcessor, times(3)).processRelay(any());
    }

    // ─── helper ───────────────────────────────────────────────────────────────

    private OrderOutbox pendingOutbox(Long id) {
        OrderOutbox outbox = mock(OrderOutbox.class);
        when(outbox.getId()).thenReturn(id);
        return outbox;
    }
}
