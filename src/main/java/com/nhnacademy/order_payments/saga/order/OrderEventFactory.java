package com.nhnacademy.order_payments.saga.order;

import com.nhnacademy.order_payments.dto.order.BookSummaryDto;
import com.nhnacademy.order_payments.dto.order.OrderSummaryDto;
import com.nhnacademy.order_payments.entity.Order;
import com.nhnacademy.order_payments.saga.event.OrderConfirmedEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class OrderEventFactory {

    public OrderConfirmedEvent create(Long userId, Order order, OrderSummaryDto dto) {

        List<Long> usedCouponIds = dto.bookList().stream()
                .map(BookSummaryDto::couponId)
                .filter(Objects::nonNull)
                .toList();

        Map<Long, Integer> bookList= dto.bookList().stream()
                .collect(Collectors.toMap(
                        BookSummaryDto::bookId,
                        BookSummaryDto::quantity
                ));

        return new OrderConfirmedEvent(
                UUID.randomUUID().toString(), // 생성과 동시에 UUID 박아줌
                order.getId(),
                userId,
                null,
                bookList,
                order.getTotalPrice(),
                dto.usedPoint(),
                dto.savedPoint(),
                usedCouponIds
        );
    }
}