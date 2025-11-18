package com.nhnacademy.order_payments.repository;

import com.nhnacademy.order_payments.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    // 특정 누군가의 장바구니 조회
    @Query("select c from Cart c join fetch c.details where c.userId = :userId")
    Cart findCartWithDetailsByUserId(@Param("userId") Long userId);

    // 특정 사용자의 장바구니 존재 여부
    boolean existsByCartId(Long userId);
}
