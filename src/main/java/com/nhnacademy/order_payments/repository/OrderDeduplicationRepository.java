package com.nhnacademy.order_payments.repository;

import com.nhnacademy.order_payments.entity.OrderDeduplicationLog;
import com.nhnacademy.order_payments.entity.PaymentDeduplicationLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderDeduplicationRepository extends JpaRepository<OrderDeduplicationLog, Long> {
    boolean existsByMessageId(String messageId);
}
