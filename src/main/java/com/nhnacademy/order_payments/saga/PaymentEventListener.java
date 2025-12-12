package com.nhnacademy.order_payments.saga;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentEventListener {
    private final PaymentEventPublisher paymentEventPublisher;

    @RabbitListener(queues = "${rabbitmq.queue.payment}")
    @Transactional
    public void handleOrderConfirmedEvent(OrderConfirmedEvent event) {
        log.info("[Payment API] ===== 주문 확정 이벤트 수신됨 =====");
        log.info("[Payment API] Order ID : {}", event.getOrderId());

        try {
            // TODO 실제 재고 차감 로직

            // ===== 로컬 트랜잭션 성공 =====
            // saga의 다음 단계를 위한 이벤트 발행
            paymentEventPublisher.publishBookDeductedEvent(event);

            log.info("[Payment API] 결제 성공");
            log.info("[Payment API] 다음 이벤트 발행 완료 : Payment API -> Order API");

        } catch(Exception e) { // 커스텀 예외 만들기!
            // TODO 재고 부족 혹은 실패 시 보상 트랜잭션 이벤트 발행
            log.error("[Payment API] ===== 결제 실패로 인한 보상 트랜잭션 시작 =====");
            log.error("[Payment API] Order ID : {}", event.getOrderId());

            throw e;  // 트랜잭션 걸려있으므로 예외 던지면 DB 트랜잭션 롤백됨
        }
//        catch(Exception e) {
//            log.error("[Payment API] 이벤트 처리 중 예상치 못한 오류 발생 : {}", e.getMessage());
//             TODO Dead Letter Queue 처리
//             ---> 근데 여기서도 보상 트랜잭션 날려야하는거 아님?
//        }

    }
}
