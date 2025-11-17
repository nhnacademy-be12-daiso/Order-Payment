package com.nhnacademy.order_payments.repository;

import com.nhnacademy.order_payments.entity.CartDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestParam;

@Repository
public interface CartDetailRepository extends JpaRepository<CartDetail, Long> {

}
