package com.nhnacademy.order_payments.saga.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.order_payments.repository.OrderDeduplicationRepository;
import com.nhnacademy.order_payments.saga.common.OrderConfirmedEvent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderEventListener {

    private final ObjectMapper objectMapper;
    private final OrderDeduplicationRepository orderDeduplicationRepository;

    @RabbitListener(queues = "${rabbitmq.queue.order}")
    @Transactional
    public void handleOrderConfirmedEvent(OrderConfirmedEvent event) {

        Long msgId = event.getOrderId();
        if(orderDeduplicationRepository.existsById(msgId)) {
            log.warn("[Book API] 중복 이벤트 수신 및 무시 : {}", msgId);
            return;
        }

        try {
            // TODO 주문 완료 로직
            // 그럼 굳이 트랜잭션을 붙일 필요가 있나?

        }
//        catch(Exception e) { // 커스텀 예외 만들기!
//             TODO 재고 부족 혹은 실패 시 보상 트랜잭션 이벤트 발행
//            log.error("[Order API] ===== 보상 트랜잭션 시작 =====");
//            log.error("[Order API] Order ID : {}", event.getOrderId());
//
//            throw e;  // 트랜잭션 걸려있으므로 예외 던지면 DB 트랜잭션 롤백됨
//        }

        catch(Exception e) {
            log.error("[Order API] 이벤트 처리 중 예상치 못한 오류 발생 : {}", e.getMessage());
            // DLQ 처리
            throw new AmqpRejectAndDontRequeueException(e.getMessage(), e);
        }

        log.info("[Order API] ===== !!!! 주문 로직 완료됨 !!!! =====");
        log.info("[Order API] Order ID : {}", event.getOrderId());

    }

}

