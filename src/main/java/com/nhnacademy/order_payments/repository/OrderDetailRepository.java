package com.nhnacademy.order_payments.repository;

import com.nhnacademy.order_payments.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
}
