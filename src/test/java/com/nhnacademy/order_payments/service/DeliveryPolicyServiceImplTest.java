package com.nhnacademy.order_payments.service;

import com.nhnacademy.order_payments.dto.request.DeliveryPolicyRequest;
import com.nhnacademy.order_payments.dto.response.DeliveryPolicyResponse;
import com.nhnacademy.order_payments.entity.DeliveryPolicy;
import com.nhnacademy.order_payments.exception.BusinessException;
import com.nhnacademy.order_payments.repository.DeliveryPolicyRepository;
import com.nhnacademy.order_payments.service.delivery.impl.DeliveryPolicyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryPolicyServiceImplTest {

    @Mock
    DeliveryPolicyRepository deliveryPolicyRepository;

    DeliveryPolicyServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DeliveryPolicyServiceImpl(deliveryPolicyRepository);
    }

    @Test
    @DisplayName("createPolicy - 저장 호출 및 값 매핑")
    void createPolicy() {
        DeliveryPolicyRequest req = new DeliveryPolicyRequest("기본", 5000L, 30000L);

        service.createPolicy(req);

        ArgumentCaptor<DeliveryPolicy> captor = ArgumentCaptor.forClass(DeliveryPolicy.class);
        verify(deliveryPolicyRepository).save(captor.capture());

        DeliveryPolicy saved = captor.getValue();
        assertEquals("기본", saved.getDeliveryPolicyName());
        assertEquals(5000L, saved.getDeliveryFee());
        assertEquals(30000L, saved.getFreeMinimumAmount());
    }

    @Test
    @DisplayName("getPolicies - 전체 조회 매핑")
    void getPolicies() {
        when(deliveryPolicyRepository.findAll()).thenReturn(List.of(
                new DeliveryPolicy("A", 5000L, 30000L),
                new DeliveryPolicy("B", 6000L, 40000L)
        ));

        List<DeliveryPolicyResponse> res = service.getPolicies();

        assertEquals(2, res.size());
        assertEquals("A", res.get(0).policyName());
        assertEquals(6000L, res.get(1).deliveryFee());
    }

    @Test
    @DisplayName("getCurrentPolicy - 최신 정책 반환")
    void getCurrentPolicy_success() {
        when(deliveryPolicyRepository.findTopByOrderByDeliveryPolicyIdDesc())
                .thenReturn(Optional.of(new DeliveryPolicy("최신", 7000L, 50000L)));

        DeliveryPolicyResponse res = service.getCurrentPolicy();

        assertEquals("최신", res.policyName());
        assertEquals(7000L, res.deliveryFee());
    }

    @Test
    @DisplayName("getCurrentPolicy - 없으면 BusinessException")
    void getCurrentPolicy_notFound() {
        when(deliveryPolicyRepository.findTopByOrderByDeliveryPolicyIdDesc())
                .thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> service.getCurrentPolicy());
        assertEquals("DELIVERY_POLICY_NOT_FOUND", ex.getCode());
    }

    @Test
    @DisplayName("modifyPolicy - 엔티티 값 변경")
    void modifyPolicy_success() {
        DeliveryPolicy policy = new DeliveryPolicy("기존", 5000L, 30000L);
        when(deliveryPolicyRepository.findById(10L)).thenReturn(Optional.of(policy));

        service.modifyPolicy(10L, new DeliveryPolicyRequest("수정", 6000L, 40000L));

        assertEquals("수정", policy.getDeliveryPolicyName());
        assertEquals(6000L, policy.getDeliveryFee());
        assertEquals(40000L, policy.getFreeMinimumAmount());
    }

    @Test
    @DisplayName("deletePolicy - 없으면 예외, 있으면 deleteById")
    void deletePolicy() {
        when(deliveryPolicyRepository.existsById(10L)).thenReturn(false);
        assertThrows(BusinessException.class, () -> service.deletePolicy(10L));

        when(deliveryPolicyRepository.existsById(10L)).thenReturn(true);
        service.deletePolicy(10L);
        verify(deliveryPolicyRepository).deleteById(10L);
    }
}
