package com.nhnacademy.order_payments.service.admin;

import com.nhnacademy.order_payments.entity.Order;
import com.nhnacademy.order_payments.entity.OrderDetail;
import com.nhnacademy.order_payments.exception.NotFoundOrderException;
import com.nhnacademy.order_payments.model.OrderDetailStatus;
import com.nhnacademy.order_payments.model.OrderStatus;
import com.nhnacademy.order_payments.repository.OrderDetailRepository;
import com.nhnacademy.order_payments.repository.OrderRepository;
import com.nhnacademy.order_payments.saga.SagaOrchestrator;
import com.nhnacademy.order_payments.saga.event.OrderRefundEvent;
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
    private final OrderDetailRepository orderDetailRepository;
    private final SagaOrchestrator sagaOrchestrator;


    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc();
    }

    // 반품 요청
    public List<OrderDetail> getRefundOrders() {
        return orderDetailRepository.findAllByOrderDetailStatus(OrderDetailStatus.RETURN_REQUESTED);
    }

    @Transactional
    public void startDelivery(Long orderId) {

        Order order = orderRepository.findById(orderId).orElseThrow(() -> new NotFoundOrderException("해당 주문이 존재하지 않습니다."));
        List<OrderDetail> orderDetails = order.getOrderDetailList();

        order.setOrderStatus(OrderStatus.IN_TRANSIT); // Order의 상태를 이렇게 만듬
        orderDetails.forEach(detail -> {
            detail.setOrderDetailStatus(OrderDetailStatus.SHIPPED);
            detail.setShippedAt(LocalDateTime.now()); // 출고일 기록
        });
    }

    @Transactional
    public void setDeliveryComplete(Long orderId) {

        Order order = orderRepository.findById(orderId).orElseThrow(() -> new NotFoundOrderException("해당 주문이 존재하지 않습니다."));
        List<OrderDetail> orderDetails = order.getOrderDetailList();

        order.setOrderStatus(OrderStatus.COMPLETED); // Order의 상태를 이렇게 만듬
        orderDetails.forEach(detail -> {
            detail.setOrderDetailStatus(OrderDetailStatus.DELIVERED);
        });
    }

    @Transactional
    public void approveRefund(Long orderDetailId) {



        OrderDetail orderDetail = orderDetailRepository.findById(orderDetailId).orElseThrow(() -> new NotFoundOrderException("해당 주문이 존재하지 않습니다."));

        // 반품 처리
        orderDetail.setOrderDetailStatus(OrderDetailStatus.RETURNED);

        Long amount = orderDetail.getPrice() * orderDetail.getQuantity() - 3000;
        OrderRefundEvent event = new OrderRefundEvent(orderDetail, amount);
        sagaOrchestrator.start(event);

        List<OrderDetail> allDetails = orderDetail.getOrder().getOrderDetailList();

        // 모든 상품이 반품 처리 됐는지 확인
        boolean isAllReturned = allDetails.stream()
                .allMatch(detail -> detail.getOrderDetailStatus() == OrderDetailStatus.RETURNED);

        if (isAllReturned) {
            orderDetail.getOrder().setOrderStatus(OrderStatus.RETURN);
        }

    }





}