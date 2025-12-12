package com.nhnacademy.order_payments.service.delivery;

import com.nhnacademy.order_payments.dto.request.DeliveryPolicyRequest;
import com.nhnacademy.order_payments.dto.response.DeliveryPolicyResponse;

import java.util.List;

public interface DeliveryPolicyService {

    // 정책 등록
    void createPolicy(DeliveryPolicyRequest request);

    // 정책 조회
    List<DeliveryPolicyResponse> getPolicies();

    // 정책 수정
    void modifyPolicy(Long deliveryPolicyId, DeliveryPolicyRequest request);

    // 정책 삭제
    void deletePolicy(Long deliveryPolicyId);

    // 현재 적용 중인 정책 조회
    DeliveryPolicyResponse getCurrentPolicy();
}
