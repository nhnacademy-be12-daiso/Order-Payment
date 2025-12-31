package com.nhnacademy.order_payments.model;

import com.nhnacademy.order_payments.entity.OrderDetail;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AdminRefundOrderDto {
    Long orderId;
    Long orderNumber;
    Long orderDetailId;
    OrderStatus orderStatus;
    OrderDetailStatus orderDetailStatus;
    LocalDateTime orderDate;

    public AdminRefundOrderDto(OrderDetail orderDetail) {
        this.orderId = orderDetail.getOrder().getId();
        this.orderNumber = orderDetail.getOrder().getOrderNumber();
        this.orderDetailId = orderDetail.getId();
        this.orderStatus = orderDetail.getOrder().getOrderStatus();
        this.orderDetailStatus = orderDetail.getOrderDetailStatus();
        this.orderDate = orderDetail.getOrder().getOrderDate();
    }
}