package com.nhnacademy.order_payments.saga;

import com.nhnacademy.order_payments.dto.order.BookSummaryDto;
import com.nhnacademy.order_payments.dto.order.OrderSummaryDto;
import com.nhnacademy.order_payments.entity.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class OrderEventFactory {

    public OrderConfirmedEvent create(Long userId, Order order, OrderSummaryDto dto) {

        List<Long> usedCouponIds = dto.bookList().stream()
                .map(BookSummaryDto::couponId)
                .filter(Objects::nonNull)
                .toList();

        return new OrderConfirmedEvent(
                order.getId(),
                userId,
                order.getTotalPrice(),
                dto.usedPoint(),
                dto.savedPoint(),
                usedCouponIds
        );
    }
}