package com.nhnacademy.order_payments.saga.event;


import com.nhnacademy.order_payments.entity.Order;
import com.nhnacademy.order_payments.entity.OrderDetail;
import com.nhnacademy.order_payments.saga.common.SagaEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderRefundEvent implements SagaEvent {

    private Long orderId; // orderDetail이여도 됨
    private Long userId;
    private Long outboxId;

    private Long bookId; // 반품 받을 책
    private Long quantity; // 책 권수
    private Long refundAmount; // 반품 처리 될 금액 (반품은 포인트로 적립됨)

    /**
     * 반품 시에는 사용 쿠폰, 사용 포인트 전부 사라지는걸 전제로 함
     */

    public OrderRefundEvent(OrderDetail orderDetail, Long refundAmount) {
        this.orderId = orderDetail.getId();
        this.userId = orderDetail.getOrder().getUserId();

        this.bookId = orderDetail.getBookId();
        this.quantity = Long.valueOf(orderDetail.getQuantity());
        this.refundAmount = refundAmount;
    }
}
