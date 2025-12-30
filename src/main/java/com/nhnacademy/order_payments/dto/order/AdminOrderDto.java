package com.nhnacademy.order_payments.dto.order;

import com.nhnacademy.order_payments.entity.Order;
import com.nhnacademy.order_payments.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AdminOrderDto {
    Long orderId;
    Long orderNumber;
    OrderStatus orderStatus;
    LocalDateTime orderDate;

    public AdminOrderDto(Order order) {
        this.orderId = order.getId();
        this.orderNumber = order.getOrderNumber();
        this.orderStatus = order.getOrderStatus();
        this.orderDate = order.getOrderDate();
    }
}
