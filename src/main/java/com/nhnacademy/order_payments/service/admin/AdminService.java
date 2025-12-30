package com.nhnacademy.order_payments.service.admin;

import com.nhnacademy.order_payments.entity.Order;
import com.nhnacademy.order_payments.entity.OrderDetail;
import com.nhnacademy.order_payments.exception.NotFoundOrderException;
import com.nhnacademy.order_payments.model.OrderDetailStatus;
import com.nhnacademy.order_payments.model.OrderStatus;
import com.nhnacademy.order_payments.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class AdminService {

    private final OrderRepository orderRepository;

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc();
    }

    @Transactional
    public void startDelivery(Long orderId) {

        Order order = orderRepository.findById(orderId).orElseThrow(() -> new NotFoundOrderException("해당 주문이 존재하지 않습니다."));
        List<OrderDetail> orderDetails = order.getOrderDetailList();

        order.setOrderStatus(OrderStatus.IN_TRANSIT);
        orderDetails.forEach(detail -> {
            detail.setOrderDetailStatus(OrderDetailStatus.SHIPPED);
            detail.setShippedAt(LocalDateTime.now());
        });
    }




}