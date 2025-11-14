package com.nhnacademy.order_payments.repository;

import com.nhnacademy.order_payments.entity.CartDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartDetailRepository extends JpaRepository<CartDetail, Long> {

}
