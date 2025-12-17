package com.nhnacademy.order_payments.repository;

import com.nhnacademy.order_payments.entity.PaymentDeduplicationLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentDeduplicationRepository extends JpaRepository<PaymentDeduplicationLog, Long> {
}
