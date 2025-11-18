package com.nhnacademy.order_payments.repository;

import com.nhnacademy.order_payments.entity.CartDetail;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartDetailRepository extends JpaRepository<CartDetail, Long> {

    // 특정 사용자의 장바구니에서 특정 책을 삭제
    @Transactional
    void removeCartDetailByBookIdAndCartUserId(Long userId, Long bookId);

    // 특정 사용자의 장바구니에서 모든 책을 삭제
    @Transactional
    void removeByCartUserId(Long userId);

    // 특정 사용자의 특정 책이 있는지 확인
    boolean existsByBookIdAndCartUserId(Long bookId, Long userId);
}
