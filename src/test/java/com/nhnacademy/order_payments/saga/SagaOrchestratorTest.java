package com.nhnacademy.order_payments.saga;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.order_payments.entity.SagaInstance;
import com.nhnacademy.order_payments.exception.FailedSerializationException;
import com.nhnacademy.order_payments.exception.NotFoundOrderException;
import com.nhnacademy.order_payments.repository.OrderOutboxRepository;
import com.nhnacademy.order_payments.repository.SagaInstanceRepository;
import com.nhnacademy.order_payments.saga.common.*;
import com.nhnacademy.order_payments.saga.event.OrderConfirmedEvent;
import com.nhnacademy.order_payments.service.order.SseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SagaOrchestratorTest {

    @Mock
    ObjectMapper objectMapper;

    @Mock
    SagaInstanceRepository instanceRepository;

    @Mock
    OrderOutboxRepository outboxRepository;

    @Mock
    ApplicationEventPublisher publisher;

    @Mock
    SseService sseService;

    SagaOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        orchestrator = new SagaOrchestrator(objectMapper, instanceRepository, outboxRepository, publisher, sseService);
    }

    // ─── start() ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("start - SagaInstance를 PROCESSING 상태로 저장한 뒤 BOOK 메시지를 발행한다")
    void start_savesPROCESSINGBeforeSendingMessage() throws JsonProcessingException {
        OrderConfirmedEvent event = confirmedEvent(1L);
        when(objectMapper.writeValueAsString(event)).thenReturn("{\"type\":\"CONFIRMED\"}");

        orchestrator.start(event);

        ArgumentCaptor<SagaInstance> captor = ArgumentCaptor.forClass(SagaInstance.class);
        verify(instanceRepository).save(captor.capture());
        assertEquals(SagaStatus.PROCESSING, captor.getValue().getSagaStatus());

        // Outbox에도 저장돼야 함 (send → outboxRepository.save)
        verify(outboxRepository).save(any());
    }

    @Test
    @DisplayName("start - JSON 직렬화 실패 시 FailedSerializationException이 던져진다")
    void start_serializationFailure_throwsException() throws JsonProcessingException {
        OrderConfirmedEvent event = confirmedEvent(1L);
        when(objectMapper.writeValueAsString(event)).thenThrow(new JsonProcessingException("fail") {});

        assertThrows(FailedSerializationException.class, () -> orchestrator.start(event));
        verify(instanceRepository, never()).save(any());
    }

    // ─── handleReply() success ────────────────────────────────────────────────

    @Test
    @DisplayName("handleReply - BOOK 성공 응답 → USER_POINTS 단계로 전진하고 DB에 저장된다")
    void handleReply_bookSuccess_advancesToUserPoints() throws JsonProcessingException {
        SagaInstance instance = sagaInstance("1", SagaStep.BOOK_CHECKOUT);
        when(instanceRepository.findById("1")).thenReturn(Optional.of(instance));

        OrderConfirmedEvent event = confirmedEvent(1L);
        when(objectMapper.readValue(any(String.class), eq(SagaEvent.class))).thenReturn(event);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        SagaReply reply = SagaReply.builder()
                .eventId("evt-1").orderId(1L).serviceName("BOOK").success(true).build();

        orchestrator.handleReply(reply);

        assertEquals(SagaStep.USER_POINTS, instance.getCurrentStep());
        verify(instanceRepository).save(instance);
    }

    @Test
    @DisplayName("handleReply - 마지막 단계(COUPON) 성공 → completeSaga 호출")
    void handleReply_couponSuccess_completesSaga() throws JsonProcessingException {
        SagaInstance instance = sagaInstance("1", SagaStep.COUPON_USE);
        when(instanceRepository.findById("1")).thenReturn(Optional.of(instance));

        SagaReply reply = SagaReply.builder()
                .eventId("evt-3").orderId(1L).serviceName("COUPON").success(true).build();

        orchestrator.handleReply(reply);

        assertEquals(SagaStatus.COMPLETED, instance.getSagaStatus());
        verify(sseService).notify("1", "COMPLETED");
    }

    @Test
    @DisplayName("handleReply - 실패 응답 → FAILED 상태 저장 후 보상 트랜잭션 시작")
    void handleReply_failure_savesFailedAndStartsCompensation() throws JsonProcessingException {
        SagaInstance instance = sagaInstance("1", SagaStep.USER_POINTS);
        when(instanceRepository.findById("1")).thenReturn(Optional.of(instance));

        OrderConfirmedEvent event = confirmedEvent(1L);
        when(objectMapper.readValue(any(String.class), eq(SagaEvent.class))).thenReturn(event);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        SagaReply reply = SagaReply.builder()
                .eventId("evt-2").orderId(1L).serviceName("USER").success(false).reason("포인트 부족").build();

        orchestrator.handleReply(reply);

        assertEquals(ServiceStatus.FAILED, instance.getUserStatus());
        // COMPENSATING 저장이 일어나야 함
        verify(instanceRepository, atLeast(1)).save(instance);
    }

    @Test
    @DisplayName("handleReply - 존재하지 않는 OrderID면 NotFoundOrderException")
    void handleReply_notFound_throws() {
        when(instanceRepository.findById("999")).thenReturn(Optional.empty());

        SagaReply reply = SagaReply.builder()
                .eventId("evt-x").orderId(999L).serviceName("BOOK").success(true).build();

        assertThrows(NotFoundOrderException.class, () -> orchestrator.handleReply(reply));
    }

    // ─── startCompensation() ─────────────────────────────────────────────────

    @Test
    @DisplayName("startCompensation - 첫 단계(BOOK) 실패 시 보상할 선행 서비스 없으므로 즉시 COMPENSATED")
    void startCompensation_noStepsToRollback_immediatelyCompensated() throws JsonProcessingException {
        SagaInstance instance = sagaInstance("2", SagaStep.BOOK_CHECKOUT);
        OrderConfirmedEvent event = confirmedEvent(2L);
        when(objectMapper.readValue(any(String.class), eq(SagaEvent.class))).thenReturn(event);

        orchestrator.startCompensation(instance, "재고 없음");

        assertEquals(SagaStatus.COMPENSATED, instance.getSagaStatus());
        verify(instanceRepository).save(instance);
        verify(sseService).notify("2", "COMPENSATED");
    }

    @Test
    @DisplayName("startCompensation - COUPON 실패 시 BOOK·USER에 rollback 메시지 발행")
    void startCompensation_withStepsToRollback_sendsRollbackMessages() throws JsonProcessingException {
        SagaInstance instance = sagaInstance("3", SagaStep.COUPON_USE);
        OrderConfirmedEvent event = confirmedEvent(3L);
        when(objectMapper.readValue(any(String.class), eq(SagaEvent.class))).thenReturn(event);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        orchestrator.startCompensation(instance, "쿠폰 오류");

        assertEquals(SagaStatus.COMPENSATING, instance.getSagaStatus());
        // 2개 step (BOOK, USER) × send() = 2번 outboxRepository.save
        verify(outboxRepository, times(2)).save(any());
    }

    // ─── handleCompensatedReply() ─────────────────────────────────────────────

    @Test
    @DisplayName("handleCompensatedReply - 모든 보상 완료 시 COMPENSATED로 전환")
    void handleCompensatedReply_allCompensated_setsCompensated() {
        SagaInstance instance = sagaInstance("4", SagaStep.BOOK_CHECKOUT);
        // 처음 단계에서 실패했으므로 다른 스텝은 PENDING → 전부 COMPENSATING 아님
        when(instanceRepository.findById("4")).thenReturn(Optional.of(instance));

        SagaReply reply = SagaReply.builder()
                .eventId("evt-c").orderId(4L).serviceName("BOOK").success(true).build();

        orchestrator.handleCompensatedReply(reply);

        assertEquals(SagaStatus.COMPENSATED, instance.getSagaStatus());
    }

    @Test
    @DisplayName("handleCompensatedReply - 보상이 진행 중인 스텝이 남아 있으면 상태를 COMPENSATED로 바꾸지 않는다")
    void handleCompensatedReply_compensatingRemaining_doesNotComplete() {
        SagaInstance instance = sagaInstance("5", SagaStep.COUPON_USE);
        instance.setBookStatus(ServiceStatus.COMPENSATED);
        instance.setUserStatus(ServiceStatus.COMPENSATING); // 아직 진행 중
        instance.setCouponStatus(ServiceStatus.FAILED);
        when(instanceRepository.findById("5")).thenReturn(Optional.of(instance));

        SagaReply reply = SagaReply.builder()
                .eventId("evt-c2").orderId(5L).serviceName("BOOK").success(true).build();

        orchestrator.handleCompensatedReply(reply);

        assertNotEquals(SagaStatus.COMPENSATED, instance.getSagaStatus());
    }

    // ─── isAllCompensated() ───────────────────────────────────────────────────

    @Test
    @DisplayName("isAllCompensated - 모든 스텝이 COMPENSATED/PENDING/FAILED이면 true")
    void isAllCompensated_allDone_returnsTrue() {
        SagaInstance instance = sagaInstance("6", SagaStep.BOOK_CHECKOUT);
        instance.setBookStatus(ServiceStatus.COMPENSATED);
        instance.setUserStatus(ServiceStatus.PENDING);
        instance.setCouponStatus(ServiceStatus.FAILED);

        assertTrue(orchestrator.isAllCompensated(instance));
    }

    @Test
    @DisplayName("isAllCompensated - 하나라도 COMPENSATING이면 false")
    void isAllCompensated_withCompensating_returnsFalse() {
        SagaInstance instance = sagaInstance("7", SagaStep.COUPON_USE);
        instance.setBookStatus(ServiceStatus.COMPENSATED);
        instance.setUserStatus(ServiceStatus.COMPENSATING); // 진행 중
        instance.setCouponStatus(ServiceStatus.FAILED);

        assertFalse(orchestrator.isAllCompensated(instance));
    }

    // ─── helper ───────────────────────────────────────────────────────────────

    private SagaInstance sagaInstance(String sagaId, SagaStep currentStep) {
        SagaInstance instance = new SagaInstance(sagaId, "{\"type\":\"CONFIRMED\",\"orderId\":" + sagaId + "}");
        instance.setCurrentStep(currentStep);
        return instance;
    }

    private OrderConfirmedEvent confirmedEvent(Long orderId) {
        return new OrderConfirmedEvent("evt-" + orderId, orderId, 1L, null,
                Map.of(100L, 1), 10000L, 0L, 100L, Collections.emptyList());
    }
}
