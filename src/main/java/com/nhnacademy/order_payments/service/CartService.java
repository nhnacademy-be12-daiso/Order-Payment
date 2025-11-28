package com.nhnacademy.order_payments.service;

import com.nhnacademy.order_payments.dto.BookDto;
import com.nhnacademy.order_payments.dto.BookInfo;
import com.nhnacademy.order_payments.entity.Cart;
import com.nhnacademy.order_payments.entity.CartDetail;
import com.nhnacademy.order_payments.exception.CartDetailNotFoundException;
import com.nhnacademy.order_payments.exception.ExternalServiceException;
import com.nhnacademy.order_payments.exception.NotFoundUserCartException;
import com.nhnacademy.order_payments.infra.BookApiClient;
import com.nhnacademy.order_payments.repository.CartDetailRepository;
import com.nhnacademy.order_payments.repository.CartRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartDetailRepository cartDetailRepository;
    private final BookApiClient bookApiClient;

    public CartService(CartRepository cartRepository,
                       CartDetailRepository cartDetailRepository,
                       BookApiClient bookApiClient // 임시
    ) {
        this.cartRepository = cartRepository;
        this.cartDetailRepository = cartDetailRepository;
        this.bookApiClient = bookApiClient;
    }

    // 회원 장바구니 담는 로직 + 업데이트 로직?
    @Transactional
    public void addCartItem(Long userId, long bookId, int quantity) {

        // 사용자 장바구니가 없다면 새로 생성
        Cart existCart = cartRepository.findByUserId(userId).orElseGet(
                () -> {
                    Cart newCart = new Cart(userId);
                    log.info("장바구니 생성 : {}", userId);
                    return cartRepository.save(newCart);
                });

        // 책이 원래 담겨 있으면 quantity만 수정
        if(cartDetailRepository.existsByBookIdAndCartUserId(bookId, userId)) {
            cartDetailRepository.updateQuantityByCartIdAndBookId(existCart.getCartId(), bookId, quantity);
            log.info("장바구니에 담겨있는 {} 도서의 수량이 {}로 변경되었습니다.", bookId, quantity);
            // ---> 똑같으면 안바꾸는 로직도 추가 필요??
        }
        else { // 책이 담겨있지 않으면 새롭게 담음
            CartDetail cartDetail = new CartDetail(existCart, bookId, quantity);

            cartDetailRepository.save(cartDetail);
            log.info("저장 완료 : {}", bookId);
        }
    }

    // update 로직은 create에 포함

    // 회원 장바구니 목록 반환
    public List<BookInfo> getCartItemList(Long userId) {
        List<CartDetail> itemList = cartRepository.findCartWithDetailsByUserId(userId)
                .map(Cart::getDetails)
                .orElse(Collections.emptyList());

        List<BookInfo> bookInfos = new ArrayList<>();

        for(CartDetail cartDetail : itemList) {
            BookDto bookDto;
            try {
                bookDto = bookApiClient.getBookInfo(cartDetail.getBookId());
            } catch(RuntimeException e) {
                throw new ExternalServiceException("통신 간 오류 발생");
            }
            BookInfo bookInfo = new BookInfo(cartDetail.getBookId(), bookDto.getTitle(),
                    bookDto.getPrice(), cartDetail.getQuantity());
            bookInfos.add(bookInfo);
        }

        return bookInfos;
    }

    // 회원 장바구니 특정 도서 반환
    public BookInfo getCartItem(Long userId, Long bookId) {
        Cart cart = cartRepository.findCartWithDetailsByUserId(userId).orElseThrow(() -> new CartDetailNotFoundException(bookId));
        CartDetail cartDetail = cartDetailRepository.findByCartCartIdAndBookId(cart.getCartId(), bookId);

        BookDto bookDto;

        try {
            bookDto = bookApiClient.getBookInfo(bookId); // ---> 예외처리 해줘야함
        } catch(RuntimeException e) {
            throw new ExternalServiceException("통신 간 오류 발생");
        }

        return new BookInfo(cartDetail.getBookId(), bookDto.getTitle(), bookDto.getPrice(), cartDetail.getQuantity());
    }

    // 회원 장바구니 일괄 삭제
    @Transactional
    public void removeCartAllItems(Long userId) {

        if(!cartRepository.existsByUserId(userId)) {
            log.warn("해당 유저의 장바구니는 존재하지 않습니다. : {}", userId);
            throw new NotFoundUserCartException(userId);
        }

        cartDetailRepository.removeByCartUserId(userId);
        log.info("해당 유저의 장바구니를 비웠습니다 : {} ", userId);
    }

    // 회원 장바구니 특정 도서 삭제
    @Transactional
    public void removeCartItem(Long userId, long bookId) {
        if(!cartDetailRepository.existsByBookIdAndCartUserId(bookId, userId)) {
            log.warn("해당 책은 장바구니에 존재하지 않습니다. : {}", bookId);
            throw new CartDetailNotFoundException(bookId);
        }

        cartDetailRepository.removeCartDetailByBookIdAndCartUserId(bookId, userId);
        log.info("해당 도서가 장바구니에서 삭제되었습니다 {} ", bookId);
    }
}
