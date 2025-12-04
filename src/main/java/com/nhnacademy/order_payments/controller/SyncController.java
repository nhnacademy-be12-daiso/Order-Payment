package com.nhnacademy.order_payments.controller;

import com.nhnacademy.order_payments.dto.BookItem;
import com.nhnacademy.order_payments.dto.SyncDto;
import com.nhnacademy.order_payments.service.cart.CartService;
import com.nhnacademy.order_payments.service.cart.SyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class SyncController {

    private final SyncService syncService;
    private final CartService cartService;

    @PostMapping("/sync")
    public void syncDB(@RequestBody SyncDto syncDto) {
        syncService.syncDB(syncDto);
        log.info("====== 동기화 완료 ======");
    }

    // 다른 컨트롤러를 파는게 나을거 같긴한데 일단 여기에 두겠음
    @GetMapping("/{userId}")
    List<BookItem> getCartList(@PathVariable Long userId) {

        // 기타 검증
        return cartService.getCartList(userId);
    }



}
