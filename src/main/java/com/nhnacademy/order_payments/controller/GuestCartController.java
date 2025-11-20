package com.nhnacademy.order_payments.controller;

import com.nhnacademy.order_payments.dto.BookInfo;
import com.nhnacademy.order_payments.service.GuestCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/guest/carts")
public class GuestCartController {

    private final GuestCartService guestCartService;
    public GuestCartController(GuestCartService guestCartService) {
        this.guestCartService = guestCartService;
    }

    // 장바구니 추가
    @PostMapping
    public ResponseEntity<Void> addCartBook(@RequestParam Long bookId,
                                            @RequestParam Integer quantity,
                                            @RequestHeader("X-Guest-ID") String guestId) {
        guestCartService.addBook(guestId, bookId, quantity);
        return ResponseEntity.ok().build();
    }
    // ----> 근데 얘가 약간 덮어쓰기 느낌이라 Update로직도 같이 함 -----> 필요시 수정

    // 장바구니 전체 목록 조회
    @GetMapping
    public ResponseEntity<Map<Long, BookInfo>> getCartItem(@RequestHeader("X-Guest-ID") String guestId) {

        Map<Long, BookInfo> bookList = guestCartService.getBookList(guestId);

        return ResponseEntity.ok().body(bookList);
    }

    // 장바구니에서 특정 도서 삭제
    @DeleteMapping
    public ResponseEntity<Void> deleteCartItem(@RequestParam Long bookId,
                                               @RequestHeader("X-Guest-ID") String guestId) {
        guestCartService.deleteBook(guestId, bookId);
        return ResponseEntity.ok().build();
    }
}
