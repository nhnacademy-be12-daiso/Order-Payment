package com.nhnacademy.order_payments.event;

import com.nhnacademy.order_payments.dto.order.BookSummaryDto;
import com.nhnacademy.order_payments.dto.order.OrderSummaryDto;
import com.nhnacademy.order_payments.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * 분산된 로컬 트랜잭션을 수행하기 위해 필요한 '확정된 최동 데이터'
 */



@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderConfirmedEvent implements Serializable {

    private Long orderId;
    private Long userId;

    // 여기 있는건 이미 다 검증이 됐음을 전제로 한다
    private Long totalAmount;
    private Long usedPoint; // 사용 포인트
    private Long savedPoint; // 적립 포인트
    private List<Long> usedCouponIds;
//    private String orderStatus;
    // ---> orderStatus를 굳이?

    public OrderConfirmedEvent(Long userId, Order order, OrderSummaryDto dto) {
        this.orderId = order.getId();
        this.userId = userId;

        this.totalAmount = order.getTotalPrice();
        this.usedPoint = dto.usedPoint();
        this.savedPoint = dto.savedPoint();
        this.usedCouponIds = dto.bookList().stream()
                .map(BookSummaryDto::couponId)
                .filter(Objects::nonNull)
                .toList();
        // ---> 각 도서에 먹여놓은 couponId를 리스트로 뽑아옴
    }
}
