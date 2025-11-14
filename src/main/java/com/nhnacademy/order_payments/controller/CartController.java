package com.nhnacademy.order_payments.controller;

import com.nhnacademy.order_payments.entity.Cart;
import com.nhnacademy.order_payments.service.CartService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // 테스트
    @PostMapping("/carts")
    public ResponseEntity<Void> addCartItem(@RequestParam Long bookId,
                                            @RequestParam Integer quantity) {
        Cart cart = new Cart();

        cartService.addCartItem(23L, bookId, quantity);
        return ResponseEntity.ok().build();
    }
}
