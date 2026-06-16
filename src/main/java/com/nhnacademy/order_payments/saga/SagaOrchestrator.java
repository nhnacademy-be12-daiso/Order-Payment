package com.nhnacademy.order_payments.saga;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.order_payments.entity.OrderOutbox;
import com.nhnacademy.order_payments.entity.SagaInstance;
import com.nhnacademy.order_payments.exception.FailedSerializationException;
import com.nhnacademy.order_payments.exception.NotFoundOrderException;
import com.nhnacademy.order_payments.repository.OrderOutboxRepository;
import com.nhnacademy.order_payments.repository.SagaInstanceRepository;
import com.nhnacademy.order_payments.saga.common.*;
import com.nhnacademy.order_payments.saga.event.OrderCompensateEvent;
import com.nhnacademy.order_payments.saga.event.OrderConfirmedEvent;
import com.nhnacademy.order_payments.saga.event.OrderOutboxCommittedEvent;
import com.nhnacademy.order_payments.service.order.SseService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class SagaOrchestrator {

    private final ObjectMapper objectMapper;
    private final SagaInstanceRepository instanceRepository;
    private final OrderOutboxRepository outboxRepository;
    private final ApplicationEventPublisher publisher;
    private final SseService sseService;

    @Transactional
    public void start(SagaEvent event) {
        try {
            SagaInstance instance = new SagaInstance(
                    event.getOrderId().toString(),
                    objectMapper.writeValueAsString(event)
            );
            instance.setSagaStatus(SagaStatus.PROCESSING); // 상태 설정 후
            instanceRepository.save(instance);             // 저장
            send(SagaTopic.BOOK_RK, event);
        } catch (JsonProcessingException e) {
            log.warn("객체 직렬화 실패");
            throw new FailedSerializationException("객체 직렬화 실패", e);
        }
    }

    public void send(String routingKey, SagaEvent event) {

        try { // outbox에다 장전하고 쏘기
            OrderOutbox outbox = new OrderOutbox(
                    event.getOrderId(),
                    "ORDER", // 메세지 출처 적기
                    SagaTopic.ORDER_EXCHANGE, // 이거 고정임
                    routingKey,
                    objectMapper.writeValueAsString(event)
            );

            outboxRepository.save(outbox);

            log.info("[Saga Outbox] {} 토픽으로 메시지 저장 완료 (OrderID: {})", routingKey, event.getOrderId());
            publisher.publishEvent(new OrderOutboxCommittedEvent(this, outbox.getId()));
            // ---> 이벤트 발행,

        } catch (JsonProcessingException e) {
            log.warn("객체 직렬화 실패");
            throw new FailedSerializationException("객체 직렬화 실패", e);
        }
    }

    @Transactional
    public void handleReply(SagaReply reply) {
        SagaInstance instance = instanceRepository.findById(reply.getOrderId().toString())
                .orElseThrow(() -> new NotFoundOrderException("SagaInstance를 찾을 수 없습니다. OrderID: " + reply.getOrderId()));

        SagaStep currentStep = instance.getCurrentStep();

        if(!reply.isSuccess()) {
            log.warn("[Saga] {} 단계 실패! 보상 트랜잭션 시작 사유 : {}", reply.getServiceName(), reply.getReason());
            currentStep.updateStatus(instance, ServiceStatus.FAILED);
            instanceRepository.save(instance); // 실패 상태 DB 반영
            startCompensation(instance, reply.getReason());
            return;
        }

        currentStep.updateStatus(instance, ServiceStatus.SUCCESS);
        SagaStep nextStep = currentStep.next();

        if(nextStep == SagaStep.FINISHED) {
            completeSaga(instance);
        } else {
            instance.setCurrentStep(nextStep);
            instanceRepository.save(instance); // 다음 단계 상태 DB 반영
            SagaEvent event = convertToEvent(instance.getPayload());
            nextStep.execute(this, event);
        }
    }

    public void startCompensation(SagaInstance instance, String reason) {
        log.warn("[Saga] 보상 트랜잭션 개시 - OrderID: {}", instance.getSagaId());

        OrderCompensateEvent rollbackEvent = new OrderCompensateEvent(UUID.randomUUID().toString(), convertToEvent(instance.getPayload()), reason);

        instance.setSagaStatus(SagaStatus.COMPENSATING);

        var stepsToRollback = Arrays.stream(SagaStep.values())
                .filter(step -> step.getOrder() < instance.getCurrentStep().getOrder())
                .filter(step -> step != SagaStep.FINISHED)
                .toList();

        if (stepsToRollback.isEmpty()) {
            // 보상할 서비스가 없으면 즉시 완료 처리 (좀비 상태 방지)
            log.info("[Saga] 보상할 내부 서비스가 없습니다. 즉시 COMPENSATED 처리 - OrderID: {}", instance.getSagaId());
            instance.setSagaStatus(SagaStatus.COMPENSATED);
            instanceRepository.save(instance);
            sseService.notify(instance.getSagaId(), "COMPENSATED");
            return;
        }

        instanceRepository.save(instance); // COMPENSATING 상태 DB 반영
        stepsToRollback.forEach(step -> {
            step.updateStatus(instance, ServiceStatus.COMPENSATING);
            this.send(step.getRollbackKey(), rollbackEvent);
        });
    }

    @Transactional
    public void handleCompensatedReply(SagaReply reply) {
        if(!reply.isSuccess()) { // 실패시
            log.error("[Saga Rollback] 보상 트랜잭션 실패함!! - 서비스: {}, OrderID: {}", reply.getServiceName(), reply.getOrderId());
            // TODO 예외처리 해줘야 할 듯
        }

        SagaInstance instance = instanceRepository.findById(reply.getOrderId().toString())
                .orElseThrow(() -> new NotFoundOrderException("SagaInstance를 찾을 수 없습니다. OrderID: " + reply.getOrderId()));

        // 방금 답장 보낸 서비스 상태 변경
        SagaStep responseStep = SagaStep.fromServiceName(reply.getServiceName());
        responseStep.updateStatus(instance, ServiceStatus.COMPENSATED);

        // 끝났는지 검사
        if(this.isAllCompensated(instance)) {
            instance.setSagaStatus(SagaStatus.COMPENSATED);
            log.info("[Saga] Saga 보상 로직 완료됨! Order ID : {}", instance.getSagaId());
        }
        // 안끝났으면 그냥 이대로 메서드 종료하고 기다리면 되나?
        // TODO 최종적인 결제 실패 응답 로직

    }

    // saga 완료
    public void completeSaga(SagaInstance instance) {
        log.info("[Saga] Saga 완료됨 Order ID : {}", instance.getSagaId());
        instance.setSagaStatus(SagaStatus.COMPLETED);
        sseService.notify(instance.getSagaId(), "COMPLETED");
    }

    // 역직렬화 메서드
    private SagaEvent convertToEvent(String payload) {
        try {
            return objectMapper.readValue(payload, SagaEvent.class); // ---> 인터페이스에 어노테이션 달아놨기 때문에 알잘딱 변환해줌
        } catch (JsonProcessingException e) {
            throw new RuntimeException("페이로드 복원 실패", e);
        }
    }

    public boolean isAllCompensated(SagaInstance instance) {
        return Arrays.stream(SagaStep.values())
                .filter(step -> step != SagaStep.FINISHED) // 가상 단계 제외
                .allMatch(step -> {
                    ServiceStatus status = step.getStatus(instance);
                    // 보상이 필요 없는 상태거나, 이미 보상이 완료된 상태여야 함
                    return status == ServiceStatus.PENDING ||
                            status == ServiceStatus.SUCCESS || // 이 경우는 로직상 있으면 안되지만 방어차원
                            status == ServiceStatus.FAILED ||
                            status == ServiceStatus.COMPENSATED;
                });
        // 즉, 하나라도 ServiceStatus.COMPENSATING 상태라면 false가 나감!
    }
}
