package com.nhnacademy.order_payments.service;

import com.nhnacademy.order_payments.dto.SyncDto;
import com.nhnacademy.order_payments.dto.SyncInfo;
import com.nhnacademy.order_payments.entity.Cart;
import com.nhnacademy.order_payments.entity.CartDetail;
import com.nhnacademy.order_payments.repository.CartDetailRepository;
import com.nhnacademy.order_payments.repository.CartRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncService {

    private final CartRepository cartRepository;
    private final CartDetailRepository cartDetailRepository;


    // 회원 장바구니 담는 로직 + 업데이트 로직?
    @Transactional
    public void syncDB(SyncDto syncDto) {

        Map<Long, List<SyncInfo>> syncInfosByUserId = syncDto.getSyncInfos().stream()
                .collect(Collectors.groupingBy(SyncInfo::userId));
        // ----> userId 별로 sync 내용을 묶음

        List<CartDetail> cartDetailsToSave = new ArrayList<>();

        // ---> 회원 별로 sync가 진행됨
        for(Map.Entry<Long, List<SyncInfo>> entry : syncInfosByUserId.entrySet()) {
            Long userId = entry.getKey();
            List<SyncInfo> syncInfos = entry.getValue();

            // 장바구니 있는지 확인하고 없으면 하나 생성
            Cart userCart = cartRepository.findByUserId(userId).orElseGet(
                    () -> {
                        Cart newCart = new Cart(userId);
                        log.info("장바구니 생성 : {}", userId);
                        return cartRepository.save(newCart);
                    });

            List<Long> bookIdsToDelete = new ArrayList<>(); // ---> 삭제할 친구들 모으는 list
            List<SyncInfo> syncInfosToUpdateOrInsert = new ArrayList<>(); // ---> save, update할 친구들 모으는 list
            boolean isClearCart = false;

            // Create, Update, Delete 분류
            for(SyncInfo syncInfo : syncInfos) {
                Long bookId = syncInfo.bookId();
                int quantity = syncInfo.quantity();

                // 전체 삭제 플래그
                if(bookId == -1) {
                    cartDetailRepository.removeByCartUserId(userId);
                    log.info("[{}] 장바구니 전체 비워짐", userId);
                    isClearCart = true;
                    break; // 더 이상 순회를 돌 필요가 없음
                    // ---> 다음 사용자로 넘어감
                }

                // 삭제 요청
                if(quantity == 0) {
                    bookIdsToDelete.add(bookId);
                } else {
                    syncInfosToUpdateOrInsert.add(syncInfo);
                }
            }

            if(isClearCart) {
                continue;  // --> 비워졌으므로 더 수행할 필요 없음
            }

            if(!bookIdsToDelete.isEmpty()) {
                cartDetailRepository.removeByBookIdInAndCartUserId(bookIdsToDelete, userId);
                log.info("[{}] 도서 일괄 삭제 : {}", userId, bookIdsToDelete);
            }

            if(!syncInfosToUpdateOrInsert.isEmpty()) {
                // --> bookId만 list로 뽑아줌
                List<Long> bookIdsToUpdateOrInsert = syncInfosToUpdateOrInsert.stream()
                        .map(SyncInfo::bookId)
                        .toList();

                // ----> 기존 DB에 있는 CartDetail을 한번에 조회
                List<CartDetail> existingDetails = cartDetailRepository.findAllByCartCartIdAndBookIdIn(userCart.getCartId(), bookIdsToUpdateOrInsert);
                Map<Long, CartDetail> existingDetailsByBookId = existingDetails.stream()
                        .collect(Collectors.toMap(CartDetail::getBookId, cd -> cd, (oldValue, newValue) -> oldValue));

                for(SyncInfo syncInfo : syncInfosToUpdateOrInsert) {
                    CartDetail existingDetail = existingDetailsByBookId.get(syncInfo.bookId());
                    if(existingDetail != null) { // 이미 DB에 데이터가 존재 ---> UPDATE 로직
                        if(existingDetail.getQuantity() != syncInfo.quantity()) { // 기존 수량이랑 다름
                            cartDetailsToSave.add(existingDetail);  // ---> 목록에 추가
                            log.info("[{}] 장바구니에 담겨있는 {} 도서의 수량이 {}로 변경되었습니다.", userId, syncInfo.bookId(), syncInfo.quantity());
                        } else {
                            log.info("[{}] 장바구니 수량 변동 없음 : {}", userId, syncInfo.bookId());
                        }
                    } else { // DB에 데이터가 존재하지 않음 ---> INSERT 로직
                        CartDetail newCartDetail = new CartDetail(userCart, syncInfo.bookId(), syncInfo.quantity());
                        cartDetailsToSave.add(newCartDetail); // --> 목록에 추가
                        log.info("[{}] 장바구니에 도서 추가 : {}", userId, syncInfo.bookId());
                    }
                }
            }

            // ----> 변경된 내용을 일괄로 꽂아넣음
            if(!cartDetailsToSave.isEmpty()) {
                cartDetailRepository.saveAll(cartDetailsToSave);
            }
        }
    }

}
