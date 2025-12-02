package com.nhnacademy.order_payments.repository;

import com.nhnacademy.order_payments.entity.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
}
