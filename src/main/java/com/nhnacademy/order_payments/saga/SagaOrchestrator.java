package com.nhnacademy.order_payments.saga;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.order_payments.entity.OrderOutbox;
import com.nhnacademy.order_payments.entity.SagaInstance;
import com.nhnacademy.order_payments.exception.FailedSerializationException;
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

@Service
@Slf4j
@RequiredArgsConstructor
public class SagaOrchestrator {

    private final ObjectMapper objectMapper;
    private final SagaInstanceRepository instanceRepository;
    private final OrderOutboxRepository outboxRepository;
    private final ApplicationEventPublisher publisher;
    private final SseService sseService;

    // 트랜잭션이 붙어야 하나?
    @Transactional
    public void start(SagaEvent event) {

        /**
         *  instance
         *  ---> saga를 관리하는 상태판과 같음
         */
        try {
            SagaInstance instance = new SagaInstance(
                    event.getOrderId().toString(),
                    objectMapper.writeValueAsString(event) // 직렬화 때려버림
            );
            // 저장
            instanceRepository.save(instance);
            send(SagaTopic.BOOK_RK, event); // book 부터 saga 시작
            instance.setSagaStatus(SagaStatus.PROCESSING); // Saga 상태 변경

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
        // 진행중인 상태판 가져옴
        SagaInstance instance = instanceRepository.findById(reply.getOrderId().toString())
                .orElseThrow(() -> new RuntimeException("여기에 커스텀 예외 꽂아넣어야함 !!!!!"));

        SagaStep currentStep = instance.getCurrentStep(); // 지금 단계

        if(!reply.isSuccess()) { // 실패한 경우 바로 보상
            log.warn("[Saga] {} 단계 실패! 보상 트랜잭션 시작 사유 : {}", reply.getServiceName(), reply.getReason());
            currentStep.updateStatus(instance, ServiceStatus.FAILED); // 서비스의 상태 변경
            startCompensation(instance, reply.getReason());
            return;
        }

        currentStep.updateStatus(instance, ServiceStatus.SUCCESS); // 서비스의 상태 변경
        SagaStep nextStep = currentStep.next();

        if(nextStep == SagaStep.FINISHED) { // 모든 단계가 끝남
            completeSaga(instance);
        } else { // 다음 단계가 남았음
            instance.setCurrentStep(nextStep); // 상태 업데이트

            OrderConfirmedEvent event = convertToEvent(instance.getPayload());
            nextStep.execute(this, event); // <<<<<<<<<<<<<<<<<<<<<<<<< 이 부분 공부 필요
        }
    }

    public void startCompensation(SagaInstance instance, String reason) {
        log.info("[Saga] 보상 트랜잭션 개시 - OrderID: {}", instance.getSagaId());

        OrderCompensateEvent rollbackEvent = new OrderCompensateEvent(convertToEvent(instance.getPayload()), reason);

        instance.setSagaStatus(SagaStatus.COMPENSATING); // 보상 시작 상태

        /**
         *  보상 트랜잭션 로직
         *  1. 모든 단계 순회
         *  2. 실패 단계보다 이전 순서인 단계만 모아서
         *  3. 상태 변경하고
         *  4. 모든 서비스에 동시에 쏨 (send() 사용)
         */
        // 보상해야 할 단계 필터링
        var stepsToRollback = Arrays.stream(SagaStep.values())
                .filter(step -> step.getOrder() < instance.getCurrentStep().getOrder())
                .filter(step -> step != SagaStep.FINISHED)
                .toList();

        // 보상할 단계가 없는 경우
        if (stepsToRollback.isEmpty()) {
            log.info("[Saga] 보상할 내부 서비스가 없습니다. 바로 결제 취소 로직으로 넘어갑니다.");
            return;
        }
        stepsToRollback.forEach(step -> {
            step.updateStatus(instance, ServiceStatus.COMPENSATING);
            this.send(step.getRollbackKey(), rollbackEvent);
        });
        /*
        Arrays.stream(SagaStep.values())
                // 2. 현재 실패한 단계보다 '작은' 순서(이미 성공했을 가능성이 있는 단계)만 필터링
                .filter(step -> step.getOrder() < instance.getCurrentStep().getOrder())
                .filter(step -> step != SagaStep.FINISHED) // FINISHED는 제외
                .forEach(step -> { // 서비스 각각 실행됨
                    log.info("[Saga Rollback] 서비스: {}, OrderID: {}", step.name(), instance.getSagaId());
                    step.updateStatus(instance, ServiceStatus.COMPENSATING);

                    this.send(step.getRollbackKey(), rollbackEvent);
                    // ----> 각각 메세지를 쏴줌
                });

         */
    }

    @Transactional
    public void handleCompensatedReply(SagaReply reply) {
        if(!reply.isSuccess()) { // 실패시
            log.error("[Saga Rollback] 보상 트랜잭션 실패함!! - 서비스: {}, OrderID: {}", reply.getServiceName(), reply.getOrderId());
            // TODO 예외처리 해줘야 할 듯
        }

        SagaInstance instance = instanceRepository.findById(reply.getOrderId().toString())
                .orElseThrow(() -> new RuntimeException("여기에 커스텀 예외 꽂아넣어야함 !!!!!"));

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
        log.info("[Saga] Saga 완료! 주문 로직 완료됨 Order ID : {}", instance.getSagaId());
        instance.setSagaStatus(SagaStatus.COMPLETED);
        sseService.notify(instance.getSagaId(), "COMPLETED");
    }

    // 역직렬화 메서드
    private OrderConfirmedEvent convertToEvent(String payload) {
        try {
            return objectMapper.readValue(payload, OrderConfirmedEvent.class);
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
