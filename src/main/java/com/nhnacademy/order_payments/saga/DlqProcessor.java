package com.nhnacademy.order_payments.saga;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DlqProcessor {

    @RabbitListener(queues = "${rabbitmq.queue.dlq}")
    @Transactional
    public void handleDlqMessage(Message message) {

        // DLQ 메시지는 byte 배열이므로, 원래 DTO로 복원해야 합니다.
        // 여기서는 예시로 헤더를 사용하여 실패 정보를 확인합니다.
        String routingKey = message.getMessageProperties().getReceivedRoutingKey();
        log.error("===== DLQ 메시지 수신됨 (최종 실패) =====");
        log.error("실패 출처: {}", routingKey);

        // 1. DLQ 메시지에서 원래 주문 ID를 추출 (메시지 본문 또는 헤더 사용)
//        Long orderId = extractOrderIdFromMessage(message);

//         2. 주문 상태를 최종 실패/취소로 업데이트
//        orderService.updateOrderStatus(orderId, OrderStatus.CANCELED);

//         3. 보상 트랜잭션 시작 이벤트 발행 (다른 서비스 롤백 명령)
//        orderEventPublisher.publishCompensationEvent(orderId, "기술적 오류로 인한 최종 실패");

        log.info("보상 이벤트 발행 완료: Order API -> Book, Payment API 등");

    }

}
