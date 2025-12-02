package com.nhnacademy.order_payments.repository;

import com.nhnacademy.order_payments.entity.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {
}
