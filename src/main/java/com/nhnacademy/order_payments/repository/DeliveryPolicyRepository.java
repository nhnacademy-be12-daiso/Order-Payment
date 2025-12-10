package com.nhnacademy.order_payments.repository;

import com.nhnacademy.order_payments.entity.DeliveryPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryPolicyRepository extends JpaRepository<DeliveryPolicy, Long> {
    DeliveryPolicy findByDeliveryPolicyName(String deliveryPolicyName);
}
