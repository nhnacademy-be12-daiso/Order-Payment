package com.nhnacademy.order_payments.saga;

import com.nhnacademy.order_payments.entity.OrderDeduplicationLog;
import com.nhnacademy.order_payments.repository.OrderDeduplicationRepository;
import com.nhnacademy.order_payments.saga.common.SagaReply;
import com.nhnacademy.order_payments.saga.common.SagaTopic;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DataIntegrityViolationException;
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

    @Transactional
    @RabbitListener(queues = SagaTopic.ORDER_QUEUE)
    public void onReply(SagaReply reply) {
        String dedupeKey = reply.getEventId() + "_" + reply.getServiceName();

        try {
            deduplicationRepository.save(new OrderDeduplicationLog(dedupeKey));
        } catch (DataIntegrityViolationException e) {
            // DB Unique 제약 위반 = 이미 처리된 메시지 → 정상 무시
            log.info("[Saga] 중복된 응답 무시 - Event ID : {}, Service : {}", reply.getEventId(), reply.getServiceName());
            return;
        }

        log.info("[Saga Reply] 응답 수신 - OrderID: {}, Service: {}, Success: {}",
                reply.getOrderId(), reply.getServiceName(), reply.isSuccess());

        sagaOrchestrator.handleReply(reply);
    }

    @Transactional
    @RabbitListener(queues = SagaTopic.ORDER_COMPENSATION_QUEUE)
    public void onCompensatedReply(SagaReply reply) {
        String dedupeKey = reply.getEventId() + "_" + reply.getServiceName() + "_COMP";

        try {
            deduplicationRepository.save(new OrderDeduplicationLog(dedupeKey));
        } catch (DataIntegrityViolationException e) {
            log.info("[Saga] 중복된 보상 응답 무시 - Event ID : {}, Service : {}", reply.getEventId(), reply.getServiceName());
            return;
        }

        log.info("[Saga Compensation Reply] 응답 수신 - OrderID: {}, Service: {}, Success: {}",
                reply.getOrderId(), reply.getServiceName(), reply.isSuccess());

        sagaOrchestrator.handleCompensatedReply(reply);
    }
}
