package com.nhnacademy.order_payments.repository;

import com.nhnacademy.order_payments.entity.CartDetail;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CartDetailRepository extends JpaRepository<CartDetail, Long> {

    // 특정 사용자의 장바구니에서 특정 책을 삭제
    void removeCartDetailByBookIdAndCartUserId(Long bookId, Long userId);

    // 특정 사용자의 장바구니에서 모든 책을 삭제
    void removeByCartUserId(Long userId);

    // 특정 사용자의 특정 책이 있는지 확인
    boolean existsByBookIdAndCartUserId(Long bookId, Long userId);

    CartDetail findByCartCartIdAndBookId(Long cartId, Long bookId);

    // 수량 변경
    @Modifying(clearAutomatically = true) // ---> 캐시 자동 비우기
    @Transactional
    @Query("update CartDetail cd set cd.quantity = :newQuantity " +
            "where cd.cart.cartId = :cartId and cd.bookId = :bookId")
    void updateQuantityByCartIdAndBookId(@Param("cartId") Long cartId,
                                     @Param("bookId") Long bookId,
                                     @Param("newQuantity") int newQuantity);
}
