package com.nhnacademy.order_payments.repository;

import com.nhnacademy.order_payments.entity.PaymentOutbox;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentOutboxRepository extends JpaRepository<PaymentOutbox, Long> {
}
