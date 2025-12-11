/*
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 * + Copyright 2025. NHN Academy Corp. All rights reserved.
 * + * While every precaution has been taken in the preparation of this resource,  assumes no
 * + responsibility for errors or omissions, or for damages resulting from the use of the information
 * + contained herein
 * + No part of this resource may be reproduced, stored in a retrieval system, or transmitted, in any
 * + form or by any means, electronic, mechanical, photocopying, recording, or otherwise, without the
 * + prior written permission.
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 */

package com.nhnacademy.order_payments.repository;

import com.nhnacademy.order_payments.entity.CartDetail;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CartDetailRepository extends JpaRepository<CartDetail, Long> {

    // 특정 사용자의 장바구니에서 특정 책을 삭제
    void removeByBookIdInAndCartUserId(List<Long> bookId, Long userId);

    // 특정 사용자의 장바구니에서 모든 책을 삭제
    void removeByCartUserId(Long userId);

    List<CartDetail> findAllByCartCartIdAndBookIdIn(Long cartId, List<Long> bookIds);

    // 특정 사용자의 특정 책이 있는지 확인
    boolean existsByBookIdAndCartUserId(Long bookId, Long userId);

    CartDetail findByCartCartIdAndBookId(Long cartId, Long bookId);

    Optional<CartDetail> findByBookIdAndCartUserId(Long bookId, Long userId);

    // 수량 변경
    @Modifying(clearAutomatically = true) // ---> 캐시 자동 비우기
    @Transactional
    @Query("update CartDetail cd set cd.quantity = :newQuantity " +
            "where cd.cart.cartId = :cartId and cd.bookId = :bookId")
    void updateQuantityByCartIdAndBookId(@Param("cartId") Long cartId,
                                         @Param("bookId") Long bookId,
                                         @Param("newQuantity") Integer newQuantity);
}
