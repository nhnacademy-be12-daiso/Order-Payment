package com.nhnacademy.order_payments.repository;

import com.nhnacademy.order_payments.entity.DeliveryDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryDetailRepository extends JpaRepository<DeliveryDetail, Long> {
}
