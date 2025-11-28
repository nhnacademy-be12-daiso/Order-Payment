package com.nhnacademy.order_payments.controller;

import com.nhnacademy.order_payments.dto.SyncDto;
import com.nhnacademy.order_payments.dto.SyncInfo;
import com.nhnacademy.order_payments.service.SyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/carts/sync")
@RequiredArgsConstructor
public class SyncController {

    private final SyncService syncService;

    @PostMapping
    public void syncDB(@RequestBody SyncDto syncDto) {
        syncService.syncDB(syncDto);
        log.info("=== 동기화 완료 ===");
    }
}
