package com.nhnacademy.order_payments.service;

import com.nhnacademy.order_payments.dto.BookDto;
import com.nhnacademy.order_payments.entity.Cart;
import com.nhnacademy.order_payments.entity.CartDetail;
import com.nhnacademy.order_payments.exception.CartDetailNotFoundException;
import com.nhnacademy.order_payments.exception.NotFoundUserCartException;
import com.nhnacademy.order_payments.infra.BookApiClient;
import com.nhnacademy.order_payments.repository.CartDetailRepository;
import com.nhnacademy.order_payments.repository.CartRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberCartService {

    private final CartRepository cartRepository;
    private final CartDetailRepository cartDetailRepository;
    private final BookApiClient bookApiClient;

    // 회원 장바구니 담는 로직 + 업데이트 로직?
    @Transactional
    public void addBook(Long userId, long bookId, int quantity) {
        Cart existCart = cartRepository.findById(userId).orElse(
                new Cart(userId)
        );
        BookDto bookDto = bookApiClient.getBookInfo(bookId); // --> 예외 처리 안해도 되는지?
        CartDetail cartDetail = new CartDetail(existCart, bookId, quantity, bookDto.getTitle(), bookDto.getPrice());

        cartDetailRepository.save(cartDetail);
        log.info("저장 완료 : {}", bookId);
    }

    // -----> 필요하면 Update 로직 생성

    // 회원 장바구니 목록 반환
    public List<CartDetail> getCartBookList(Long userId) {
        Cart cart = cartRepository.findCartWithDetailsByUserId(userId);
        return cart.getDetails();
    }

    // 회원 장바구니 특정 도서 반환
    public CartDetail getCartBook(Long userId, Long bookId) {
        return cartDetailRepository.findByBookIdAndCartUserId(userId, bookId);
        // ----> 검증 로직 필요하지 않나?
    }

    // 회원 장바구니 일괄 삭제
    public void deleteAllCartBook(Long userId) {

        if(!cartRepository.existsByCartId(userId)) {
            throw new NotFoundUserCartException(userId);
        }

        cartDetailRepository.removeByCartUserId(userId);
        log.info("해당 유저의 장바구니를 비웠습니다 : {} ", userId);
    }

    // 회원 장바구니 특정 도서 삭제
    public void deleteCartBook(Long userId, long bookId) {
        if(!cartDetailRepository.existsByBookIdAndCartUserId(userId, bookId)) {
            throw new CartDetailNotFoundException(bookId);
        }

        cartDetailRepository.removeCartDetailByBookIdAndCartUserId(userId, bookId);
        log.info("도서가 삭제되었습니다 {} ", bookId);
    }

    // 간단한 검증 로직
    private void validate(Long userId, long bookId, int quantity) {
         
    }


}
