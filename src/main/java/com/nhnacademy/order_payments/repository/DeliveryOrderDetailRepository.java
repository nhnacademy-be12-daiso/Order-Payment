package com.nhnacademy.order_payments.repository;

import com.nhnacademy.order_payments.entity.DeliveryOrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryOrderDetailRepository extends JpaRepository<DeliveryOrderDetail, Long> {
}
