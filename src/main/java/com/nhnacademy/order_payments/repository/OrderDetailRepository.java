package com.nhnacademy.order_payments.repository;

import com.nhnacademy.order_payments.entity.OrderDetail;
import com.nhnacademy.order_payments.model.OrderDetailStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    List<OrderDetail> findAllByOrderDetailStatus(OrderDetailStatus status);
}
