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

@Tag(name = "Delivery Policy", description = "배송비 정책 관리 (관리자용)")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/admin/deliveries/policies")
public class DeliveryPolicyController {

    private final DeliveryPolicyService deliveryPolicyService;

    @PostMapping
    @Operation(summary = "배송비 정책 생성", description = "새로운 배송비 정책을 생성합니다.")
    public ResponseEntity<Void> createPolicy(@RequestHeader("X-User-Id") Long adminId,
                                             @Valid @RequestBody DeliveryPolicyRequest request) {
        deliveryPolicyService.createPolicy(request);
        log.info("관리자 [{}] - 배송비 정책 등록", adminId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    @Operation(summary = "배송비 정책 조회", description = "배송비 정책을 모두 조회합니다.")
    public ResponseEntity<List<DeliveryPolicyResponse>> getPolicies(@RequestHeader("X-User-Id") Long adminId) {
        List<DeliveryPolicyResponse> responses = deliveryPolicyService.getPolicies();
        log.info("관리자 [{}] - 배송비 정책 목록 조회", adminId);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{deliveryPolicyId}")
    @Operation(summary = "배송비 정책 수정", description = "배송비 정책을 수정합니다.")
    public ResponseEntity<Void> modifyPolicy(@RequestHeader("X-User-Id") Long adminId,
                                             @PathVariable Long deliveryPolicyId,
                                             @Valid @RequestBody DeliveryPolicyRequest request) {
        deliveryPolicyService.modifyPolicy(deliveryPolicyId, request);
        log.info("관리자 [{}] - 배송비 정책 수정 id={}", adminId, deliveryPolicyId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{deliveryPolicyId}")
    @Operation(summary = "배송비 정책 삭제", description = "배송비 정책을 삭제합니다.")
    public ResponseEntity<Void> deletePolicy(@RequestHeader("X-User-Id") Long adminId,
                                             @PathVariable Long deliveryPolicyId) {
        deliveryPolicyService.deletePolicy(deliveryPolicyId);
        log.info("관리자 [{}] - 배송비 정책 삭제 id={}", adminId, deliveryPolicyId);
        return ResponseEntity.noContent().build();
    }

}
