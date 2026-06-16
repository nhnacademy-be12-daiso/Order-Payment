package com.nhnacademy.order_payments.saga;

import com.nhnacademy.order_payments.entity.OrderDeduplicationLog;
import com.nhnacademy.order_payments.repository.OrderDeduplicationRepository;
import com.nhnacademy.order_payments.saga.common.SagaReply;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderReplyListenerTest {

    @Mock
    SagaOrchestrator sagaOrchestrator;

    @Mock
    OrderDeduplicationRepository deduplicationRepository;

    OrderReplyListener listener;

    @BeforeEach
    void setUp() {
        listener = new OrderReplyListener(sagaOrchestrator, deduplicationRepository);
    }

    // ─── onReply() ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("onReply - 최초 수신 시 중복 로그를 저장하고 orchestrator.handleReply를 호출한다")
    void onReply_firstTime_delegatesToOrchestrator() {
        SagaReply reply = reply("evt-1", 1L, "BOOK", true);

        listener.onReply(reply);

        verify(deduplicationRepository).save(any(OrderDeduplicationLog.class));
        verify(sagaOrchestrator).handleReply(reply);
    }

    @Test
    @DisplayName("onReply - 동일 eventId+serviceName 재수신 시 orchestrator를 호출하지 않는다")
    void onReply_duplicate_ignoresMessage() {
        SagaReply reply = reply("evt-1", 1L, "BOOK", true);
        doThrow(new DataIntegrityViolationException("duplicate"))
                .when(deduplicationRepository).save(any());

        listener.onReply(reply);

        verify(sagaOrchestrator, never()).handleReply(any());
    }

    @Test
    @DisplayName("onReply - 실패 응답도 중복 키가 없으면 정상 처리된다")
    void onReply_failureReply_delegatesToOrchestrator() {
        SagaReply reply = reply("evt-2", 2L, "USER", false);

        listener.onReply(reply);

        verify(sagaOrchestrator).handleReply(reply);
    }

    // ─── onCompensatedReply() ─────────────────────────────────────────────────

    @Test
    @DisplayName("onCompensatedReply - 최초 수신 시 orchestrator.handleCompensatedReply를 호출한다")
    void onCompensatedReply_firstTime_delegatesToOrchestrator() {
        SagaReply reply = reply("evt-c1", 1L, "BOOK", true);

        listener.onCompensatedReply(reply);

        verify(deduplicationRepository).save(any(OrderDeduplicationLog.class));
        verify(sagaOrchestrator).handleCompensatedReply(reply);
    }

    @Test
    @DisplayName("onCompensatedReply - 중복 수신 시 orchestrator를 호출하지 않는다")
    void onCompensatedReply_duplicate_ignoresMessage() {
        SagaReply reply = reply("evt-c1", 1L, "USER", true);
        doThrow(new DataIntegrityViolationException("duplicate"))
                .when(deduplicationRepository).save(any());

        listener.onCompensatedReply(reply);

        verify(sagaOrchestrator, never()).handleCompensatedReply(any());
    }

    @Test
    @DisplayName("onCompensatedReply - 일반 응답과 보상 응답의 중복 키는 서로 다르다")
    void onReply_and_onCompensatedReply_useDifferentDedupeKeys() {
        SagaReply reply = reply("evt-same", 1L, "BOOK", true);

        listener.onReply(reply);
        listener.onCompensatedReply(reply);

        // 두 번 save 호출 (키가 달라 중복으로 처리되지 않아야 함)
        verify(deduplicationRepository, times(2)).save(any(OrderDeduplicationLog.class));
        verify(sagaOrchestrator).handleReply(reply);
        verify(sagaOrchestrator).handleCompensatedReply(reply);
    }

    // ─── helper ───────────────────────────────────────────────────────────────

    private SagaReply reply(String eventId, Long orderId, String serviceName, boolean success) {
        return SagaReply.builder()
                .eventId(eventId)
                .orderId(orderId)
                .serviceName(serviceName)
                .success(success)
                .build();
    }
}
