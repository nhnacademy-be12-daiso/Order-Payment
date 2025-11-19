package com.nhnacademy.order_payments.repository;

import com.nhnacademy.order_payments.entity.Order;
import com.nhnacademy.order_payments.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentKey(String paymentKey);
    Optional<Payment> findByOrder(Order order);
    boolean existsByPaymentKey(String paymentKey);
}
