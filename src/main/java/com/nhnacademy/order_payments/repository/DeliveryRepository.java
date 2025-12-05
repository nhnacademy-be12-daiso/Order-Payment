package com.nhnacademy.order_payments.repository;

import com.nhnacademy.order_payments.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
}
