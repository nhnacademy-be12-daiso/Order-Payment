package com.nhnacademy.order_payments.repository;

import com.nhnacademy.order_payments.entity.OrderOutbox;
import com.nhnacademy.order_payments.model.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderOutboxRepository extends JpaRepository<OrderOutbox, Long> {
    List<OrderOutbox> findByStatus(OutboxStatus status);
}
