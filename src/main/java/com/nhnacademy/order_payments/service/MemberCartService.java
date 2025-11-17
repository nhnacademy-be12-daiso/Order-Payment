package com.nhnacademy.order_payments.service;

import com.nhnacademy.order_payments.dto.BookDto;
import com.nhnacademy.order_payments.entity.Cart;
import com.nhnacademy.order_payments.entity.CartDetail;
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

    @Transactional
    public void addCartItem(Long userId, long bookId, int quantity) {
        Cart existCart = cartRepository.findById(userId).orElse(
                new Cart(userId)
        );
        BookDto bookDto = bookApiClient.getBookInfo(bookId); // --> 예외 처리 안해도 되는지?
        CartDetail cartDetail = new CartDetail(existCart, bookId, quantity, bookDto.getTitle(), bookDto.getPrice());

        cartDetailRepository.save(cartDetail);
        log.info("저장 완료 : {}", bookId);
    }

    public List<CartDetail> getCartItemList(Long userId) {
        Cart cart = cartRepository.findCartWithDetailsByUserId(userId);
        return cart.getDetails();
    }

    public void validate(Long userId, long bookId, int quantity) {
         
    }

}
