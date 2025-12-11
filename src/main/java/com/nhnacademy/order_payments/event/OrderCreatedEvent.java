package com.nhnacademy.order_payments.event;

import com.nhnacademy.order_payments.entity.OrderDetail;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * [결제하기] 버튼을 누른 후 가장 먼저 발생하는 '검증' 이벤트
 * ---> 주문 정보 자체를 담고 있음
 *
 * ** 검증 메커니즘 **
 * 1. 각 서비스와 다시 통신해서 실제 데이터와 맞는지 검사(유효성 검사)
 * 2. 최종 결제 금액 다시 계산해보고, 맞는지 확인
 *
 * Q. 왜 해야하나?
 * A. 중간에 값이 달라졌을수도 있고, 사용자가 주작했을수도 있음
 * ----> 프론트에서 오는 값은 일단 믿지마라;;
 * --> 그리고 돈에 관련된거라 확실해야 함
 *
 * 이건 saga와는 별개인 로컬 트랜잭션임
 */

// 큰 의미는 없고 그냥 로깅용 이벤트
public class OrderCreatedEvent implements Serializable {

    private String eventId;
    private Instant timestamp; // 이벤트 발생 시점 기록

    private Long orderId;
    private Long userId;
    private String receiverAddress;
    private BigDecimal expectedTotalAmount; // 사용자가 화면에서 본 예상 결제 금액

    private List<OrderDetail> Items;

    private Long selectedCouponId;
    private Long inputPoint;
}
