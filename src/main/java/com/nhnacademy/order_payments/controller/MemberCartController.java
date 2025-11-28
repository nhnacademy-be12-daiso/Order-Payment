package com.nhnacademy.order_payments.controller;

import com.nhnacademy.order_payments.dto.BookInfo;
import com.nhnacademy.order_payments.service.CartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/member/carts")
public class MemberCartController {

    private final CartService cartService;
    public MemberCartController(CartService cartService) {
        this.cartService = cartService;
    }

    // 장바구니 추가
    @PostMapping
    public ResponseEntity<Void> addCartBook(@RequestParam Long bookId,
                                            @RequestParam Integer quantity,
                                            @RequestHeader("X-Member-ID") Long userId) {
        cartService.addCartItem(userId, bookId, quantity);
        return ResponseEntity.ok().build();
    }

//    // 장바구니 전체 조회
    @GetMapping
    public ResponseEntity<List<BookInfo>> getCartBookList(@RequestHeader("X-Member-ID") Long userId) {

        List<BookInfo> bookList = cartService.getCartItemList(userId);
        if(bookList.isEmpty()) {
            log.info("장바구니가 비어있습니다.");
        }

        return ResponseEntity.ok().body(bookList);
    }

    // 장바구니 특정 책 조회
    @GetMapping("{bookId}")
    public ResponseEntity<BookInfo> getCartBook(@PathVariable Long bookId,
                                                @RequestHeader("X-Member-ID") Long userId) {
        BookInfo book = cartService.getCartItem(userId, bookId);
        return ResponseEntity.ok().body(book);
    }

//     장바구니 일부 삭제
    @DeleteMapping("/{bookId}")
    public ResponseEntity<Void> deleteCartBook(@PathVariable Long bookId,
                                               @RequestHeader("X-Member-ID") Long userId) {
        cartService.removeCartItem(userId, bookId);
        return ResponseEntity.ok().build();
    }

    // 장바구니 전체 삭제
    @DeleteMapping
    public ResponseEntity<Void> deleteCartAll(@RequestHeader("X-Member-ID") Long userId) {
        cartService.removeCartAllItems(userId);
        return ResponseEntity.ok().build();
    }


}
