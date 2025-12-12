package com.nhnacademy.order_payments.service.delivery.impl;

import com.nhnacademy.order_payments.dto.request.DeliveryPolicyRequest;
import com.nhnacademy.order_payments.dto.response.DeliveryPolicyResponse;
import com.nhnacademy.order_payments.entity.DeliveryPolicy;
import com.nhnacademy.order_payments.exception.BusinessException;
import com.nhnacademy.order_payments.repository.DeliveryPolicyRepository;
import com.nhnacademy.order_payments.service.delivery.DeliveryPolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class DeliveryPolicyServiceImpl implements DeliveryPolicyService {

    private final DeliveryPolicyRepository deliveryPolicyRepository;

    @Override
    @Transactional
    public void createPolicy(DeliveryPolicyRequest request) {
        DeliveryPolicy policy = new DeliveryPolicy(
                request.policyName(),
                request.deliveryFee(),
                request.freeMinimumAmount()
        );

        deliveryPolicyRepository.save(policy);

        log.info("배송비 정책 추가 - name: {}, fee: {}, freeMinimumAmount: {}",
                request.policyName(), request.deliveryFee(), request.freeMinimumAmount());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryPolicyResponse> getPolicies() {
        return deliveryPolicyRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryPolicyResponse getCurrentPolicy() {
        DeliveryPolicy policy = deliveryPolicyRepository.findTopByOrderByDeliveryPolicyIdDesc()
                .orElseThrow(() -> new BusinessException("DELIVERY_POLICY_NOT_FOUND", "등록된 배송비 정책이 없습니다."));

        return toResponse(policy);
    }

    @Override
    @Transactional
    public void modifyPolicy(Long deliveryPolicyId, DeliveryPolicyRequest request) {
        DeliveryPolicy policy = deliveryPolicyRepository.findById(deliveryPolicyId)
                .orElseThrow(() -> new BusinessException("DELIVERY_POLICY_NOT_FOUND", "배송비 정책을 찾을 수 없습니다."));

        policy.modifyPolicy(
                request.policyName(),
                request.deliveryFee(),
                request.freeMinimumAmount()
        );

        log.info("배송비 정책 수정 - id: {}", deliveryPolicyId);
    }

    @Override
    @Transactional
    public void deletePolicy(Long deliveryPolicyId) {
        if (!deliveryPolicyRepository.existsById(deliveryPolicyId)) {
            throw new BusinessException("DELIVERY_POLICY_NOT_FOUND", "배송비 정책을 찾을 수 없습니다.");
        }

        deliveryPolicyRepository.deleteById(deliveryPolicyId);
        log.info("배송비 정책 삭제 - id: {}", deliveryPolicyId);
    }

    private DeliveryPolicyResponse toResponse(DeliveryPolicy policy) {
        return new DeliveryPolicyResponse(
                policy.getDeliveryPolicyId(),
                policy.getDeliveryPolicyName(),
                policy.getDeliveryFee(),
                policy.getFreeMinimumAmount()
        );
    }


}
