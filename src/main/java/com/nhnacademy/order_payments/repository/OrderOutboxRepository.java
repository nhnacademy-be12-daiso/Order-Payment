package com.nhnacademy.order_payments.repository;


import com.nhnacademy.order_payments.entity.OrderOutbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderOutboxRepository extends JpaRepository<OrderOutbox, Long> {
}
