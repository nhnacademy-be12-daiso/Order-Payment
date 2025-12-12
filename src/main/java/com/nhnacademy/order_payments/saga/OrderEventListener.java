package com.nhnacademy.order_payments.saga;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderEventListener {

    @RabbitListener(queues = "${rabbitmq.queue.order}")
    @Transactional
    public void handleOrderConfirmedEvent(OrderConfirmedEvent event) {
        log.info("[Order API] ===== !!!! 주문 로직 완료됨 !!!! =====");
        log.info("[Order API] Order ID : {}", event.getOrderId());

        try {
            // TODO 실제 재고 차감 로직

        } catch(Exception e) { // 커스텀 예외 만들기!
            // TODO 재고 부족 혹은 실패 시 보상 트랜잭션 이벤트 발행
            log.error("[Order API] ===== 보상 트랜잭션 시작 =====");
            log.error("[Order API] Order ID : {}", event.getOrderId());

            throw e;  // 트랜잭션 걸려있으므로 예외 던지면 DB 트랜잭션 롤백됨
        }
//        catch(Exception e) {
//            log.error("[Order API] 이벤트 처리 중 예상치 못한 오류 발생 : {}", e.getMessage());
//             TODO Dead Letter Queue 처리
//             ---> 근데 여기서도 보상 트랜잭션 날려야하는거 아님?
//        }

    }

}

