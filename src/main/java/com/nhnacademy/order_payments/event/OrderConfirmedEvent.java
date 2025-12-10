package com.nhnacademy.order_payments.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderConfirmedEvent implements Serializable {

    private Long orderId;
    private Long userId;

    // 여기 있는건 이미 다 검증이 됐음을 전제로 한다
    private BigDecimal totalAmount;
    private Long usedPoint;
    private Long usedCouponId;
    private String orderStatus;
}
