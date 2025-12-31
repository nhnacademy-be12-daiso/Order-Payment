package com.nhnacademy.order_payments.saga;

import com.nhnacademy.order_payments.entity.OrderDeduplicationLog;
import com.nhnacademy.order_payments.repository.OrderDeduplicationRepository;
import com.nhnacademy.order_payments.saga.common.SagaReply;
import com.nhnacademy.order_payments.saga.common.SagaTopic;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


/**
 * 얘는 그냥 Service로부터 날라오는 Reply를 잡아채서 Orchestrator로 넘겨주는 일만 한다.
 * 그 외 로직은 추가하지 말것
 * ---> 로직은 Orchestrator에 추가하기
 */


@Slf4j
@Component
@RequiredArgsConstructor
public class OrderReplyListener {

    private final SagaOrchestrator sagaOrchestrator;
    private final OrderDeduplicationRepository deduplicationRepository;

    // 성공 트랜잭션에 대한 답변을 받는 리스너
    @Transactional
    @RabbitListener(queues = SagaTopic.ORDER_QUEUE)
    public void onReply(SagaReply reply) {

        String dedupeKey = reply.getEventId() + "_" + reply.getServiceName();

        if(deduplicationRepository.existsByMessageId(dedupeKey)) { // 이미 존재한다면?
            log.info("[Saga] 중복된 응답 무시 - Event ID : {}, Service : {}", reply.getEventId(), reply.getServiceName());
            return;
        }

        log.info("[Saga Reply] 응답 수신 - OrderID: {}, Service: {}, Success: {}",
                reply.getOrderId(), reply.getServiceName(), reply.isSuccess());

        // 멱등성 보장
        OrderDeduplicationLog logEntry = new OrderDeduplicationLog(dedupeKey);
        deduplicationRepository.save(logEntry);

        sagaOrchestrator.handleReply(reply);

    }

    @Transactional
    @RabbitListener(queues = SagaTopic.ORDER_COMPENSATION_QUEUE)
    public void onCompensatedReply(SagaReply reply) {

        // 주문ID + 서비스 + 상태
        String dedupeKey = reply.getEventId() + "_" + reply.getServiceName() + "_COMP";

        // 중복 검사
        if(deduplicationRepository.existsByMessageId(dedupeKey)) { // 이미 존재한다면?
            log.info("[Saga] 중복된 보상 응답 무시 - Event ID : {}, Service : {}", reply.getEventId(), reply.getServiceName());
            return;
        }

        log.info("[Saga Compensation Reply] 응답 수신 - OrderID: {}, Service: {}, Success: {}",
                reply.getOrderId(), reply.getServiceName(), reply.isSuccess());

        // 멱등성 보장
        deduplicationRepository.save(new OrderDeduplicationLog(dedupeKey));

        // 상태 업데이트는 오케스트레이터가 함
        sagaOrchestrator.handleCompensatedReply(reply);
    }
}
