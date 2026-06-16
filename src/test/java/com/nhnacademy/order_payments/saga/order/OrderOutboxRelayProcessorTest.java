package com.nhnacademy.order_payments.saga.order;

import com.nhnacademy.order_payments.entity.OrderOutbox;
import com.nhnacademy.order_payments.exception.ExternalServiceException;
import com.nhnacademy.order_payments.model.OutboxStatus;
import com.nhnacademy.order_payments.repository.OrderOutboxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderOutboxRelayProcessorTest {

    @Mock
    OrderEventPublisher orderEventPublisher;

    @Mock
    OrderOutboxRepository orderOutboxRepository;

    OrderOutboxRelayProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new OrderOutboxRelayProcessor(orderEventPublisher, orderOutboxRepository);
    }

    // ─── 성공 케이스 ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("발행 성공 시 Outbox 상태가 PUBLISHED로 변경되고 저장된다")
    void processRelay_success_marksAsPublished() {
        OrderOutbox outbox = outboxWithRetry(1L, 0);
        when(orderOutboxRepository.findById(1L)).thenReturn(Optional.of(outbox));

        processor.processRelay(1L);

        verify(orderEventPublisher).publishOrderOutboxMessage(any(), any(), any());
        verify(outbox).markAsPublished();
        verify(orderOutboxRepository).save(outbox);
    }

    // ─── 실패 케이스 (재시도) ─────────────────────────────────────────────────

    @Test
    @DisplayName("발행 실패(retryCount < 3)시 retryCount를 증가시키고 예외를 외부로 던지지 않는다")
    void processRelay_failureUnderRetryLimit_incrementsRetryCountWithoutThrowing() {
        OrderOutbox outbox = outboxWithRetry(2L, 0);
        when(orderOutboxRepository.findById(2L)).thenReturn(Optional.of(outbox));
        doThrow(new ExternalServiceException("RabbitMQ down"))
                .when(orderEventPublisher).publishOrderOutboxMessage(any(), any(), any());

        assertDoesNotThrow(() -> processor.processRelay(2L));

        verify(outbox).incrementRetryCount();
        verify(orderOutboxRepository).save(outbox);
        verify(outbox, never()).markAsFailed();
    }

    @Test
    @DisplayName("retryCount가 2(마지막 허용 재시도)일 때도 retryCount를 증가하고 예외를 던지지 않는다")
    void processRelay_failureAtRetry2_incrementsRetryCount() {
        OrderOutbox outbox = outboxWithRetry(3L, 2);
        when(orderOutboxRepository.findById(3L)).thenReturn(Optional.of(outbox));
        doThrow(new ExternalServiceException("fail"))
                .when(orderEventPublisher).publishOrderOutboxMessage(any(), any(), any());

        assertDoesNotThrow(() -> processor.processRelay(3L));

        verify(outbox).incrementRetryCount();
        verify(outbox, never()).markAsFailed();
    }

    // ─── 최종 실패 ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("retryCount가 3 이상이면 FAILED로 마킹하고 예외를 던지지 않는다")
    void processRelay_failureAtMaxRetry_marksAsFailed() {
        OrderOutbox outbox = outboxWithRetry(4L, 3);
        when(orderOutboxRepository.findById(4L)).thenReturn(Optional.of(outbox));
        doThrow(new ExternalServiceException("fail"))
                .when(orderEventPublisher).publishOrderOutboxMessage(any(), any(), any());

        assertDoesNotThrow(() -> processor.processRelay(4L));

        verify(outbox).markAsFailed();
        verify(outbox, never()).incrementRetryCount();
        verify(orderOutboxRepository).save(outbox);
    }

    @Test
    @DisplayName("발행 실패 시 markAsPublished가 호출되지 않는다")
    void processRelay_failure_doesNotMarkPublished() {
        OrderOutbox outbox = outboxWithRetry(5L, 0);
        when(orderOutboxRepository.findById(5L)).thenReturn(Optional.of(outbox));
        doThrow(new ExternalServiceException("fail"))
                .when(orderEventPublisher).publishOrderOutboxMessage(any(), any(), any());

        processor.processRelay(5L);

        verify(outbox, never()).markAsPublished();
    }

    // ─── 재시도 카운터 보존 검증 ──────────────────────────────────────────────

    @Test
    @DisplayName("발행 실패 후 예외가 전파되지 않으므로 REQUIRES_NEW 트랜잭션이 커밋되어 retryCount가 보존된다")
    void processRelay_noExceptionPropagated_transactionCommits() {
        OrderOutbox outbox = outboxWithRetry(6L, 1);
        when(orderOutboxRepository.findById(6L)).thenReturn(Optional.of(outbox));
        doThrow(new ExternalServiceException("fail"))
                .when(orderEventPublisher).publishOrderOutboxMessage(any(), any(), any());

        // 예외가 밖으로 나오면 REQUIRES_NEW 트랜잭션이 롤백되어 retryCount 증가가 취소됨
        // 예외가 나오지 않아야 retryCount가 DB에 보존됨
        assertDoesNotThrow(() -> processor.processRelay(6L));
    }

    // ─── helper ───────────────────────────────────────────────────────────────

    private OrderOutbox outboxWithRetry(Long id, int retryCount) {
        OrderOutbox outbox = mock(OrderOutbox.class);
        when(outbox.getId()).thenReturn(id);
        when(outbox.getTopic()).thenReturn("team3.saga.exchange");
        when(outbox.getRoutingKey()).thenReturn("command.book.checkout");
        when(outbox.getPayload()).thenReturn("{\"type\":\"CONFIRMED\"}");
        when(outbox.getRetryCount()).thenReturn(retryCount);
        return outbox;
    }
}
