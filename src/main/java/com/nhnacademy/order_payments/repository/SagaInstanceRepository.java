package com.nhnacademy.order_payments.repository;

import com.nhnacademy.order_payments.entity.SagaInstance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SagaInstanceRepository extends JpaRepository<SagaInstance, String> {
}
