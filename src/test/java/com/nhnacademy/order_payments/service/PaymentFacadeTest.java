package com.nhnacademy.order_payments.service;

import com.nhnacademy.order_payments.dto.request.CancelRequest;
import com.nhnacademy.order_payments.dto.request.ConfirmRequest;
import com.nhnacademy.order_payments.dto.request.FailRequest;
import com.nhnacademy.order_payments.dto.request.RefundRequest;
import com.nhnacademy.order_payments.dto.response.ConfirmResponse;
import com.nhnacademy.order_payments.dto.response.RefundResponse;
import com.nhnacademy.order_payments.entity.Order;
import com.nhnacademy.order_payments.entity.Payment;
import com.nhnacademy.order_payments.entity.PaymentHistory;
import com.nhnacademy.order_payments.exception.BusinessException;
import com.nhnacademy.order_payments.model.PaymentEventType;
import com.nhnacademy.order_payments.model.PaymentMethod;
import com.nhnacademy.order_payments.payment.provider.PaymentProvider;
import com.nhnacademy.order_payments.payment.service.PaymentFacade;
import com.nhnacademy.order_payments.repository.OrderRepository;
import com.nhnacademy.order_payments.repository.PaymentHistoryRepository;
import com.nhnacademy.order_payments.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentFacadeTest {

    @Mock
    OrderRepository orderRepository;

    @Mock
    PaymentRepository paymentRepository;

    @Mock
    PaymentHistoryRepository paymentHistoryRepository;

    @Mock
    PaymentProvider paymentProvider;

    PaymentFacade paymentFacade;

    @BeforeEach
    void setUp() {
        paymentFacade = new PaymentFacade(
                orderRepository,
                paymentRepository,
                paymentHistoryRepository,
                paymentProvider
        );
    }

    @Test
    @DisplayName("confirm - 정상 승인 흐름")
    void confirm_success() {
        // given
        Long userId = 1L;
        String orderIdStr = "1001";
        int amount = 50_000;
        String paymentKey = "payment_key_123";

        Order order = mock(Order.class);
        when(order.getOrderNumber()).thenReturn(1001L);
        when(order.getTotalPrice()).thenReturn(amount);

        when(orderRepository.findById(1001L))
                .thenReturn(Optional.of(order));

        when(paymentRepository.findByOrder(order))
                .thenReturn(Optional.empty());

        PaymentProvider.ApproveResult approveResult =
                new PaymentProvider.ApproveResult(
                        "TOSS",
                        "CARD",
                        "2025-12-01T12:00:00+09:00"
                );
        when(paymentProvider.approve(any(PaymentProvider.ApproveCommand.class)))
                .thenReturn(approveResult);

        ConfirmRequest request = new ConfirmRequest(
                "TOSS",
                orderIdStr,
                paymentKey,
                amount
        );

        // when
        ConfirmResponse response = paymentFacade.confirm(userId, request);

        // then
        assertEquals("1001", response.orderId());
        assertEquals("PAID", response.status());
        assertEquals("CARD", response.method());

        verify(paymentRepository).save(any(Payment.class));
        verify(paymentHistoryRepository).save(any(PaymentHistory.class));
    }

    @Test
    @DisplayName("confirm - 결제 금액이 주문 금액과 다르면 BusinessException 발생")
    void confirm_amountMismatch_throwsBusinessException() {
        // given
        Long userId = 1L;
        String orderIdStr = "1001";

        Order order = mock(Order.class);
        when(order.getOrderNumber()).thenReturn(1001L);
        when(order.getTotalPrice()).thenReturn(30_000); // 주문 금액

        when(orderRepository.findById(1001L))
                .thenReturn(Optional.of(order));

        // 여기서는 금액 불일치에서 바로 예외가 나가므로
        // paymentRepository.findByOrder(...) stub 은 필요 없음

        ConfirmRequest request = new ConfirmRequest(
                "TOSS",
                orderIdStr,
                "payment_key_123",
                50_000 // 요청 금액이 다름
        );

        // when
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> paymentFacade.confirm(userId, request)
        );

        // then
        assertEquals("AMOUNT_MISMATCH", ex.getCode());
        verify(paymentProvider, never()).approve(any());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("confirm - 이미 결제가 존재하면 PG 재호출 없이 기존 결제 정보로 응답")
    void confirm_duplicateRequest_returnsExistingPayment() {
        // given
        Long userId = 1L;
        String orderIdStr = "1002";
        int amount = 70_000;
        String paymentKey = "existing_key";

        Order order = mock(Order.class);
        when(order.getOrderNumber()).thenReturn(1002L);
        when(order.getTotalPrice()).thenReturn(amount);

        when(orderRepository.findById(1002L))
                .thenReturn(Optional.of(order));

        OffsetDateTime approvedAt = OffsetDateTime.now();
        Payment existingPayment = Payment.builder()
                .order(order)
                .paymentCost(amount)
                .paymentKey(paymentKey)
                .paymentMethod(PaymentMethod.CARD)
                .pgProvider("TOSS")
                .cardIssuerCode(null)
                .build();
        existingPayment.setApprovedAt(approvedAt);

        when(paymentRepository.findByOrder(order))
                .thenReturn(Optional.of(existingPayment));

        ConfirmRequest request = new ConfirmRequest(
                "TOSS",
                orderIdStr,
                paymentKey,
                amount
        );

        // when
        ConfirmResponse response = paymentFacade.confirm(userId, request);

        // then
        assertEquals("1002", response.orderId());
        assertEquals("PAID", response.status());
        assertEquals("CARD", response.method());
        assertEquals(approvedAt, response.approvedAt());

        verify(paymentProvider, never()).approve(any());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("cancel - 전액 취소 시 CANCEL 이력 남김")
    void cancel_fullAmount_success() {
        // given
        Long userId = 1L;
        String orderIdStr = "2001";
        int paidAmount = 40_000;
        String paymentKey = "pay_key_cancel";

        Order order = mock(Order.class);
        when(order.getOrderNumber()).thenReturn(2001L);
        // cancel()에서는 order.getTotalPrice()를 사용하지 않으므로 stub 불필요

        when(orderRepository.findById(2001L))
                .thenReturn(Optional.of(order));

        Payment payment = Payment.builder()
                .order(order)
                .paymentCost(paidAmount)
                .paymentKey(paymentKey)
                .paymentMethod(PaymentMethod.CARD)
                .pgProvider("TOSS")
                .cardIssuerCode(null)
                .build();

        when(paymentRepository.findByOrder(order))
                .thenReturn(Optional.of(payment));

        PaymentProvider.CancelResult cancelResult =
                new PaymentProvider.CancelResult("TOSS", "CARD", "2025-12-02T10:00:00+09:00");
        when(paymentProvider.cancel(any(PaymentProvider.CancelCommand.class)))
                .thenReturn(cancelResult);

        CancelRequest request = new CancelRequest(
                orderIdStr,
                paymentKey,
                "단순 변심",
                null   // null 이면 전액 취소
        );

        // when
        var response = paymentFacade.cancel(userId, request);

        // then
        assertEquals("2001", response.orderId());
        assertEquals("CANCELED", response.status());
        assertEquals("CARD", response.method());

        ArgumentCaptor<PaymentHistory> captor = ArgumentCaptor.forClass(PaymentHistory.class);
        verify(paymentHistoryRepository).save(captor.capture());

        PaymentHistory history = captor.getValue();
        assertEquals(PaymentEventType.CANCEL, history.getEventType());
        assertEquals(paidAmount, history.getAmount());
    }

    @Test
    @DisplayName("refund - 환불 시 REFUND 이력 남김")
    void refund_success() {
        // given
        Long userId = 1L;
        String orderIdStr = "3001";
        int paidAmount = 60_000;
        String paymentKey = "pay_key_refund";

        Order order = mock(Order.class);
        when(order.getOrderNumber()).thenReturn(3001L);
        // refund()에서도 order.getTotalPrice()는 사용하지 않음

        when(orderRepository.findById(3001L))
                .thenReturn(Optional.of(order));

        Payment payment = Payment.builder()
                .order(order)
                .paymentCost(paidAmount)
                .paymentKey(paymentKey)
                .paymentMethod(PaymentMethod.CARD)
                .pgProvider("TOSS")
                .cardIssuerCode(null)
                .build();

        when(paymentRepository.findByOrder(order))
                .thenReturn(Optional.of(payment));

        PaymentProvider.CancelResult cancelResult =
                new PaymentProvider.CancelResult("TOSS", "CARD", "2025-12-03T10:00:00+09:00");
        when(paymentProvider.cancel(any(PaymentProvider.CancelCommand.class)))
                .thenReturn(cancelResult);

        RefundRequest request = new RefundRequest(
                orderIdStr,
                paymentKey,
                "상품 문제",
                30_000   // 부분 환불
        );

        // when
        RefundResponse response = paymentFacade.refund(userId, request);

        // then
        assertEquals("3001", response.orderId());
        assertEquals("REFUNDED", response.status());
        assertEquals("CARD", response.method());

        ArgumentCaptor<PaymentHistory> captor = ArgumentCaptor.forClass(PaymentHistory.class);
        verify(paymentHistoryRepository).save(captor.capture());

        PaymentHistory history = captor.getValue();
        assertEquals(PaymentEventType.REFUND, history.getEventType());
        assertEquals(30_000, history.getAmount());
        assertEquals("상품 문제", history.getReason());
    }

    @Test
    @DisplayName("fail - 결제 실패 정보가 FAIL 이력으로 저장됨")
    void fail_savesHistory() {
        // given
        FailRequest request = new FailRequest(
                "4001",
                10_000,
                "NO_KEY",
                "ERROR_CODE",
                "에러 메시지"
        );

        // when
        paymentFacade.fail(request);

        // then
        ArgumentCaptor<PaymentHistory> captor = ArgumentCaptor.forClass(PaymentHistory.class);
        verify(paymentHistoryRepository).save(captor.capture());

        PaymentHistory history = captor.getValue();
        assertEquals(PaymentEventType.FAIL, history.getEventType());
        assertEquals(10_000, history.getAmount());
        assertTrue(history.getReason().contains("ERROR_CODE"));
    }
}
