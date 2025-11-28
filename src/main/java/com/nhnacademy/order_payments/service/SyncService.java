package com.nhnacademy.order_payments.service;

import com.nhnacademy.order_payments.dto.SyncDto;
import com.nhnacademy.order_payments.dto.SyncInfo;
import com.nhnacademy.order_payments.entity.Cart;
import com.nhnacademy.order_payments.entity.CartDetail;
import com.nhnacademy.order_payments.infra.BookApiClient;
import com.nhnacademy.order_payments.repository.CartDetailRepository;
import com.nhnacademy.order_payments.repository.CartRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncService {

    private final CartRepository cartRepository;
    private final CartDetailRepository cartDetailRepository;


    // 회원 장바구니 담는 로직 + 업데이트 로직?
    @Transactional
    public void syncDB(SyncDto syncDto) {

        for(SyncInfo syncInfo : syncDto.getSyncInfos()) {
            Long userId = syncInfo.userId();
            Long bookId = syncInfo.bookId();
            int quantity = syncInfo.quantity();

            // 사용자 장바구니가 없다면 새로 생성
            Cart existCart = cartRepository.findByUserId(userId).orElseGet(
                    () -> {
                        Cart newCart = new Cart(userId);
                        log.info("장바구니 생성 : {}", userId);
                        return cartRepository.save(newCart);
                    });

            Optional<CartDetail> existingDetailOpt = cartDetailRepository.findByBookIdAndCartUserId(bookId, userId);

            if(bookId == -1) { // 전체 삭제 플래그
                cartDetailRepository.removeByCartUserId(userId);
                log.info("[{}] 장바구니 전체 비워짐", userId);
                break; // 더 이상 순회를 돌 필요가 없음
            }


            if(quantity == 0) { // 삭제 요청 들어온 애들
                cartDetailRepository.removeCartDetailByBookIdAndCartUserId(bookId, userId);
                log.info("[{}] 도서 삭제됨 : {}", userId, bookId);
                continue;
            }


            if (existingDetailOpt.isPresent()) {
                CartDetail existingDetail = existingDetailOpt.get();

                // 기존 수량과 같으면 스킵
                if (existingDetail.getQuantity() == quantity) {
                    log.info("[{}] 장바구니 수량 변동 없음 : {}", userId, bookId);
                    continue; // 다음 루프로 넘어갑니다. (UPDATE 쿼리 방지)
                }

                // 수량 차이가 있으면 수행
                cartDetailRepository.updateQuantityByCartIdAndBookId(existCart.getCartId(), bookId, quantity);
                log.info("[{}] 장바구니에 담겨있는 {} 도서의 수량이 {}로 변경되었습니다.", userId, bookId, quantity);
            } else { // 책이 담겨있지 않으면 새롭게 담음
                CartDetail cartDetail = new CartDetail(existCart, bookId, quantity);
                cartDetailRepository.save(cartDetail);
                log.info("[{}] 장바구니에 도서 추가 : {}", userId, bookId);
            }
        }
    }

}
