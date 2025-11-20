package com.nhnacademy.order_payments.controller;

import com.nhnacademy.order_payments.dto.BookInfo;
import com.nhnacademy.order_payments.entity.CartDetail;
import com.nhnacademy.order_payments.service.MemberCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/member/carts")
public class MemberCartController {

    private final MemberCartService memberCartService;
    public MemberCartController(MemberCartService memberCartService) {
        this.memberCartService = memberCartService;
    }

    // 장바구니 추가
    @PostMapping
    public ResponseEntity<Void> addCartBook(@RequestParam Long bookId,
                                            @RequestParam Integer quantity,
                                            @RequestHeader("X-Member-ID") Long userId) {
        memberCartService.addBook(userId, bookId, quantity);
        return ResponseEntity.ok().build();
    }

    // 장바구니 전체 조회
    @GetMapping
    public ResponseEntity<List<CartDetail>> getCartBookList(@RequestHeader("X-Member-ID") Long userId) {

        List<CartDetail> bookList = memberCartService.getCartBookList(userId);

        return ResponseEntity.ok().body(bookList);
    }

    // 장바구니 특정 책 조회
    @GetMapping("{bookId}")
    public ResponseEntity<BookInfo> getCartBook(@PathVariable Long bookId,
                                                  @RequestHeader("X-Member-ID") Long userId) {
        BookInfo book = memberCartService.getCartBook(userId, bookId);
        return ResponseEntity.ok().body(book);
    }

    // 장바구니 업데이트

    // 장바구니 전체 삭제
    @DeleteMapping("/{bookId}")
    public ResponseEntity<Void> deleteCartBook(@PathVariable Long bookId,
                                               @RequestHeader("X-Member-ID") Long userId) {

        memberCartService.deleteCartBook(userId, bookId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/all")
    public ResponseEntity<Void> deleteCartAll(@RequestHeader("X-Member-ID") Long userId) {

        memberCartService.deleteAllCartBook(userId);
        return ResponseEntity.ok().build();
    }


}
