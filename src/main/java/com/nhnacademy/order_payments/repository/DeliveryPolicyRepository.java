package com.nhnacademy.order_payments.repository;

import com.nhnacademy.order_payments.entity.DeliveryPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeliveryPolicyRepository extends JpaRepository<DeliveryPolicy, Long> {

    // 가장 최근에 추가된 배송 정책 조회
    Optional<DeliveryPolicy> findTopByOrderByDeliveryPolicyIdDesc();
}
