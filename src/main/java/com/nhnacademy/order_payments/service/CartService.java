package com.nhnacademy.order_payments.service;

import com.nhnacademy.order_payments.entity.Cart;
import com.nhnacademy.order_payments.entity.CartDetail;
import com.nhnacademy.order_payments.repository.CartDetailRepository;
import com.nhnacademy.order_payments.repository.CartRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartDetailRepository cartDetailRepository;


    @Transactional
    public void addCartItem(Long ordererId, long bookId, int quantity) {

        Cart cart = cartRepository.findById(ordererId)
                .orElseGet(() -> {
                    Cart newCart = new Cart(ordererId);
                    return cartRepository.save(newCart);
                });

        CartDetail cartDetail = new CartDetail(cart, bookId, quantity);
        cartDetailRepository.save(cartDetail);
        log.info("저장 완료");
    }


}
