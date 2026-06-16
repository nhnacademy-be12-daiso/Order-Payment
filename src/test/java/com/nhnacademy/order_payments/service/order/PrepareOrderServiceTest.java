package com.nhnacademy.order_payments.service.order;

import com.nhnacademy.order_payments.client.BookApiClient;
import com.nhnacademy.order_payments.client.CouponApiClient;
import com.nhnacademy.order_payments.client.UserApiClient;
import com.nhnacademy.order_payments.dto.order.*;
import com.nhnacademy.order_payments.entity.DeliveryPolicy;
import com.nhnacademy.order_payments.exception.ExternalServiceException;
import com.nhnacademy.order_payments.exception.NotFoundOrderException;
import com.nhnacademy.order_payments.repository.DeliveryPolicyRepository;
import com.nhnacademy.order_payments.repository.PackagingRepository;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrepareOrderServiceTest {

    @Mock BookApiClient bookApiClient;
    @Mock UserApiClient userApiClient;
    @Mock CouponApiClient couponApiClient;
    @Mock PackagingRepository packagingRepository;
    @Mock DeliveryPolicyRepository deliveryPolicyRepository;

    PrepareOrderService service;

    @BeforeEach
    void setUp() {
        service = new PrepareOrderService(bookApiClient, userApiClient, couponApiClient,
                packagingRepository, deliveryPolicyRepository);
    }

    // ─── prepareOrderInfo (회원) ─────────────────────────────────────────────

    @Test
    @DisplayName("회원 주문서 - 모든 외부 호출 성공 시 PrepareOrderDto 반환")
    void prepareOrderInfo_success_returnsPrepareOrderDto() {
        stubAllApis(1L);

        PrepareOrderDto result = service.prepareOrderInfo(1L, List.of(new PrepareOrderRequest(100L, 1)));

        assertNotNull(result);
        verify(bookApiClient).getBookInfos(any());
        verify(userApiClient).getUserInfo(1L);
        verify(couponApiClient).getAvailableCoupons(1L);
        verify(packagingRepository).findAllByEnabled(true);
        verify(deliveryPolicyRepository).findTopByOrderByDeliveryPolicyIdDesc();
    }

    @Test
    @DisplayName("회원 주문서 - Book API FeignException 발생 시 ExternalServiceException 변환")
    void prepareOrderInfo_feignException_throwsExternalServiceException() {
        when(bookApiClient.getBookInfos(any())).thenThrow(mock(FeignException.class));
        when(deliveryPolicyRepository.findTopByOrderByDeliveryPolicyIdDesc())
                .thenReturn(Optional.of(dummyDeliveryPolicy()));
        when(packagingRepository.findAllByEnabled(true)).thenReturn(List.of());

        assertThrows(ExternalServiceException.class,
                () -> service.prepareOrderInfo(1L, List.of(new PrepareOrderRequest(100L, 1))));
    }

    @Test
    @DisplayName("회원 주문서 - 배송 정책 없으면 NotFoundOrderException")
    void prepareOrderInfo_noDeliveryPolicy_throwsNotFoundOrderException() {
        when(bookApiClient.getBookInfos(any())).thenReturn(dummyBooksResponse());
        when(deliveryPolicyRepository.findTopByOrderByDeliveryPolicyIdDesc()).thenReturn(Optional.empty());
        when(packagingRepository.findAllByEnabled(true)).thenReturn(List.of());

        assertThrows(NotFoundOrderException.class,
                () -> service.prepareOrderInfo(1L, List.of(new PrepareOrderRequest(100L, 1))));
    }

    // ─── prepareGuestOrderInfo (비회원) ──────────────────────────────────────

    @Test
    @DisplayName("비회원 주문서 - 성공 시 PrepareOrderDto 반환 (회원 정보·쿠폰 없음)")
    void prepareGuestOrderInfo_success_returnsPrepareOrderDto() {
        when(bookApiClient.getBookInfos(any())).thenReturn(dummyBooksResponse());
        when(deliveryPolicyRepository.findTopByOrderByDeliveryPolicyIdDesc())
                .thenReturn(Optional.of(dummyDeliveryPolicy()));
        when(packagingRepository.findAllByEnabled(true)).thenReturn(List.of());

        PrepareOrderDto result = service.prepareGuestOrderInfo(List.of(new PrepareOrderRequest(100L, 1)));

        assertNotNull(result);
        // 비회원이므로 User·Coupon API는 호출되지 않아야 함
        verify(userApiClient, never()).getUserInfo(any());
        verify(couponApiClient, never()).getAvailableCoupons(any());
    }

    @Test
    @DisplayName("비회원 주문서 - FeignException 발생 시 ExternalServiceException 변환")
    void prepareGuestOrderInfo_feignException_throwsExternalServiceException() {
        when(bookApiClient.getBookInfos(any())).thenThrow(mock(FeignException.class));
        when(deliveryPolicyRepository.findTopByOrderByDeliveryPolicyIdDesc())
                .thenReturn(Optional.of(dummyDeliveryPolicy()));
        when(packagingRepository.findAllByEnabled(true)).thenReturn(List.of());

        assertThrows(ExternalServiceException.class,
                () -> service.prepareGuestOrderInfo(List.of(new PrepareOrderRequest(100L, 1))));
    }

    // ─── helper ───────────────────────────────────────────────────────────────

    private void stubAllApis(Long userId) {
        when(bookApiClient.getBookInfos(any())).thenReturn(dummyBooksResponse());
        when(deliveryPolicyRepository.findTopByOrderByDeliveryPolicyIdDesc())
                .thenReturn(Optional.of(dummyDeliveryPolicy()));
        when(packagingRepository.findAllByEnabled(true)).thenReturn(List.of());
        when(userApiClient.getUserInfo(userId))
                .thenReturn(ResponseEntity.ok(new UserInfoResponse(
                        userId, "테스터", "010-0000-0000", "test@test.com",
                        "SILVER", BigDecimal.ZERO, 1000L, List.of())));
        when(couponApiClient.getAvailableCoupons(userId))
                .thenReturn(ResponseEntity.ok(List.of()));
    }

    private InternalBooksInfoResponse dummyBooksResponse() {
        InternalBookInfoResponse book = new InternalBookInfoResponse(
                100L, "자바의 정석", 30_000L, 10, Status.ON_SALE,
                BigDecimal.ZERO, 30_000L, "img.jpg", 1, true
        );
        return new InternalBooksInfoResponse(List.of(book));
    }

    private DeliveryPolicy dummyDeliveryPolicy() {
        return new DeliveryPolicy("기본", 3_000L, 30_000L);
    }
}
