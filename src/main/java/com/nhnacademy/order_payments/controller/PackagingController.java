/*
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 * + Copyright 2025. NHN Academy Corp. All rights reserved.
 * + * While every precaution has been taken in the preparation of this resource,  assumes no
 * + responsibility for errors or omissions, or for damages resulting from the use of the information
 * + contained herein
 * + No part of this resource may be reproduced, stored in a retrieval system, or transmitted, in any
 * + form or by any means, electronic, mechanical, photocopying, recording, or otherwise, without the
 * + prior written permission.
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 */

package com.nhnacademy.order_payments.controller;

import com.nhnacademy.order_payments.dto.packaging.request.PackagingRequest;
import com.nhnacademy.order_payments.dto.packaging.response.PackagingResponse;
import com.nhnacademy.order_payments.service.packaging.PackagingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "포장 정책 API - 관리자 전용")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/admin/packagings")
public class PackagingController {

    private final PackagingService packagingService;

    // POST /api/admin/packagings
    @PostMapping
    @Operation(summary = "포장 정책 등록")
    public ResponseEntity<Void> createPackaging(@RequestHeader("X-User-Id") Long adminId,
                                                @Valid @RequestBody PackagingRequest request) {
        packagingService.createPackaging(request);
        log.info("관리자 [{}] - 포장 정책 등록", adminId);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // GET /api/admin/packagings
    @GetMapping
    @Operation(summary = "포장 정책 전체 조회")
    public ResponseEntity<List<PackagingResponse>> getPackagings() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(packagingService.getPackagings());
    }

    // PUT /api/admin/packagings/{packagingId}
    @PutMapping("/{packagingId}")
    @Operation(summary = "포장 정책 수정")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 포장 정책")
    public ResponseEntity<Void> modifyPackaging(@RequestHeader("X-User-Id") Long adminId,
                                                @PathVariable Long packagingId,
                                                @Valid @RequestBody PackagingRequest request) {
        packagingService.modifyPackaging(packagingId, request);
        log.info("관리자 [{}] - 포장 정책 수정, id: {}", adminId, packagingId);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // DELETE /api/admin/packagings/{packagingId}
    @DeleteMapping("/{packagingId}")
    @Operation(summary = "포장 정책 삭제")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 포장 정책")
    public ResponseEntity<Void> deletePackaging(@RequestHeader("X-User-Id") Long adminId,
                                                @PathVariable Long packagingId) {
        packagingService.deletePackaging(packagingId);
        log.info("관리자 [{}] - 포장 정책 삭제, id: {}", adminId, packagingId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
