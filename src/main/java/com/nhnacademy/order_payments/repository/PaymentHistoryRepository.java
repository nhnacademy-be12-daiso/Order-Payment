package com.nhnacademy.order_payments.repository;

import com.nhnacademy.order_payments.entity.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {
    // 결제에 대한 히스토리 전체 조회
    List<PaymentHistory> findByPaymentOrderOrderNumberOrderByPaymentTimeAsc(Long orderNumber);

    // payment 기준
    List<PaymentHistory> findByPaymentOrderOrderIdOrderByPaymentTimeAsc(Long orderId);
}
