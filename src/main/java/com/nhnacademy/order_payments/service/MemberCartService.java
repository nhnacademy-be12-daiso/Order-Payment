package com.nhnacademy.order_payments.service;

import com.nhnacademy.order_payments.dto.BookDto;
import com.nhnacademy.order_payments.dto.BookInfo;
import com.nhnacademy.order_payments.entity.Cart;
import com.nhnacademy.order_payments.entity.CartDetail;
import com.nhnacademy.order_payments.exception.CartDetailNotFoundException;
import com.nhnacademy.order_payments.exception.NotFoundUserCartException;
import com.nhnacademy.order_payments.infra.BookApiClient;
import com.nhnacademy.order_payments.repository.CartDetailRepository;
import com.nhnacademy.order_payments.repository.CartRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class MemberCartService {

    private final CartRepository cartRepository;
    private final CartDetailRepository cartDetailRepository;
    private final BookApiClient bookApiClient;

    public MemberCartService(CartRepository cartRepository,
                             CartDetailRepository cartDetailRepository,
                             BookApiClient bookApiClient // 임시
    ) {
        this.cartRepository = cartRepository;
        this.cartDetailRepository = cartDetailRepository;
        this.bookApiClient = bookApiClient;
    }

    // 회원 장바구니 담는 로직 + 업데이트 로직?
    @Transactional
    public void addBook(Long userId, long bookId, int quantity) {

        Cart existCart = cartRepository.findByUserId(userId).orElseGet(
                () -> {
                    Cart newCart = new Cart(userId);
                    return cartRepository.save(newCart);
                });

        if(cartDetailRepository.existsByBookIdAndCartUserId(userId, bookId)) { // 책이 원래 담겨 있으면 quantity만 수정
            cartDetailRepository.updateQuantityByCartDetailId(existCart.getCartId(), bookId, quantity);
            log.info("장바구니에 담겨있는 {} 도서의 수량이 {}로 변경되었습니다.", bookId, quantity);
            // ---> 똑같으면 안바꾸는 로직도 추가 필요
        }
        else {
            BookDto bookDto;
            try {
                bookDto = bookApiClient.getBookInfo(bookId); // --> 예외 처리 안해도 되는지?
            } catch (Exception e) {
                log.warn("일단 이 시점에서 문제임");
                throw new RuntimeException("에바임;;");
            }
            CartDetail cartDetail = new CartDetail(existCart, bookId, quantity);

            cartDetailRepository.save(cartDetail);
            log.info("저장 완료 : {}", bookId);
        }
    }

    // -----> 필요하면 Update 로직 생성

    // 회원 장바구니 목록 반환
    public List<CartDetail> getCartBookList(Long userId) {
        Cart cart = cartRepository.findCartWithDetailsByUserId(userId);
        return cart.getDetails();
    }

    // 회원 장바구니 특정 도서 반환
    public BookInfo getCartBook(Long userId, Long bookId) {
        Cart cart = cartRepository.findCartWithDetailsByUserId(userId);
        CartDetail cartDetail = cartDetailRepository.findByCartCartIdAndBookId(cart.getCartId(), bookId);

        return new BookInfo(cartDetail.getBookId(), cartDetail.getTitle(), cartDetail.getPrice(), cartDetail.getQuantity());
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
        log.info("해당 도서가 장바구니에서 삭제되었습니다 {} ", bookId);
    }

    // 간단한 검증 로직
    private void validate(Long userId, long bookId, int quantity) {
         
    }


}
