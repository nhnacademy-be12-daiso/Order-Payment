package com.nhnacademy.order_payments.controller;

import com.nhnacademy.order_payments.dto.request.DeliveryPolicyRequest;
import com.nhnacademy.order_payments.dto.response.DeliveryPolicyResponse;
import com.nhnacademy.order_payments.service.delivery.DeliveryPolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "배송비 정책 API - 관리자 전용")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/admin/deliveries/policies")
public class DeliveryPolicyController {

    private final DeliveryPolicyService deliveryPolicyService;

    @PostMapping
    @Operation(summary = "배송비 정책 등록")
    public ResponseEntity<Void> createPolicy(@RequestHeader("X-User-Id") Long adminId,
                                             @Valid @RequestBody DeliveryPolicyRequest request) {
        deliveryPolicyService.createPolicy(request);
        log.info("관리자 [{}] - 배송비 정책 등록", adminId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    @Operation(summary = "배송비 정책 전체 조회")
    public ResponseEntity<List<DeliveryPolicyResponse>> getPolicies(@RequestHeader("X-User-Id") Long adminId) {
        List<DeliveryPolicyResponse> responses = deliveryPolicyService.getPolicies();
        log.info("관리자 [{}] - 배송비 정책 목록 조회", adminId);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{deliveryPolicyId}")
    @Operation(summary = "배송비 정책 수정")
    public ResponseEntity<Void> modifyPolicy(@RequestHeader("X-User-Id") Long adminId,
                                             @PathVariable Long deliveryPolicyId,
                                             @Valid @RequestBody DeliveryPolicyRequest request) {
        deliveryPolicyService.modifyPolicy(deliveryPolicyId, request);
        log.info("관리자 [{}] - 배송비 정책 수정 id={}", adminId, deliveryPolicyId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{deliveryPolicyId}")
    @Operation(summary = "배송비 정책 삭제")
    public ResponseEntity<Void> deletePolicy(@RequestHeader("X-User-Id") Long adminId,
                                             @PathVariable Long deliveryPolicyId) {
        deliveryPolicyService.deletePolicy(deliveryPolicyId);
        log.info("관리자 [{}] - 배송비 정책 삭제 id={}", adminId, deliveryPolicyId);
        return ResponseEntity.noContent().build();
    }

}
