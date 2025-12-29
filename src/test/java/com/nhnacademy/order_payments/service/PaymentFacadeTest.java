package com.nhnacademy.order_payments.service;

import com.nhnacademy.order_payments.dto.payment.request.CancelRequest;
import com.nhnacademy.order_payments.dto.payment.request.ConfirmRequest;
import com.nhnacademy.order_payments.dto.payment.request.FailRequest;
import com.nhnacademy.order_payments.dto.payment.request.RefundRequest;
import com.nhnacademy.order_payments.dto.payment.response.ConfirmResponse;
import com.nhnacademy.order_payments.dto.payment.response.RefundResponse;
import com.nhnacademy.order_payments.entity.Order;
import com.nhnacademy.order_payments.entity.Payment;
import com.nhnacademy.order_payments.entity.PaymentHistory;
import com.nhnacademy.order_payments.exception.BusinessException;
import com.nhnacademy.order_payments.model.PaymentEventType;
import com.nhnacademy.order_payments.model.PaymentMethod;
import com.nhnacademy.order_payments.provider.PaymentProvider;
import com.nhnacademy.order_payments.service.payment.PaymentFacade;
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

// 결제 로직 테스트 코드
// 승인/취소/환불/실패/기록이 잘 수행되는지 검증
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
        Long userId = 1L;
        String orderIdStr = "1001";
        Long amount = 50_000L;
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
                        "2025-12-01T12:00:00+09:00",
                        "TOSSPAY"
                );
        when(paymentProvider.approve(any(PaymentProvider.ApproveCommand.class)))
                .thenReturn(approveResult);

        ConfirmRequest request = new ConfirmRequest(
                "TOSS",
                orderIdStr,
                paymentKey,
                amount
        );

        ConfirmResponse response = paymentFacade.confirm(userId, request);

        assertEquals("1001", response.orderId());
        assertEquals("PAID", response.status());
        assertEquals("CARD", response.method());

        verify(paymentRepository).save(any(Payment.class));
        verify(paymentHistoryRepository).save(any(PaymentHistory.class));
    }

    @Test
    @DisplayName("confirm - 결제 금액이 주문 금액과 다르면 BusinessException 발생")
    void confirm_amountMismatch_throwsBusinessException() {
        Long userId = 1L;
        String orderIdStr = "1001";

        Order order = mock(Order.class);
        when(order.getOrderNumber()).thenReturn(1001L);
        when(order.getTotalPrice()).thenReturn(30_000L); // 주문 금액

        when(orderRepository.findById(1001L))
                .thenReturn(Optional.of(order));

        // 여기서는 금액 불일치에서 바로 예외가 나가므로
        // paymentRepository.findByOrder() stub 은 필요x

        ConfirmRequest request = new ConfirmRequest(
                "TOSS",
                orderIdStr,
                "payment_key_123",
                50_000L // 요청 금액이 다름
        );

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> paymentFacade.confirm(userId, request)
        );

        assertEquals("AMOUNT_MISMATCH", ex.getCode());
        verify(paymentProvider, never()).approve(any());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("confirm - 이미 결제가 존재하면 PG 재호출 없이 기존 결제 정보로 응답")
    void confirm_duplicateRequest_returnsExistingPayment() {
        Long userId = 1L;
        String orderIdStr = "1002";
        Long amount = 70_000L;
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

        ConfirmResponse response = paymentFacade.confirm(userId, request);

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
        Long userId = 1L;
        String orderIdStr = "2001";
        Long paidAmount = 40_000L;
        String paymentKey = "pay_key_cancel";

        Order order = mock(Order.class);
        when(order.getOrderNumber()).thenReturn(2001L);

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

        var response = paymentFacade.cancel(userId, request);

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
    @DisplayName("cancel - 부분 취소 시 PARTIAL_CANCEL 이력 남김")
    void cancel_partialAmount_savesPartialCancelHistory() {
        Long userId = 1L;
        String orderIdStr = "2001";
        Long paidAmount = 40_000L;

        Order order = mock(Order.class);
        when(order.getOrderNumber()).thenReturn(2001L);
        when(orderRepository.findById(2001L)).thenReturn(Optional.of(order));

        Payment payment = Payment.builder()
                .order(order)
                .paymentCost(paidAmount)
                .paymentKey("db_payment_key")
                .paymentMethod(PaymentMethod.CARD)
                .pgProvider("TOSS")
                .cardIssuerCode(null)
                .build();

        when(paymentRepository.findByOrder(order)).thenReturn(Optional.of(payment));
        when(paymentProvider.cancel(any(PaymentProvider.CancelCommand.class)))
                .thenReturn(new PaymentProvider.CancelResult("TOSS", "CARD", "2025-12-02T10:00:00+09:00"));

        CancelRequest request = new CancelRequest(orderIdStr, "db_payment_key", "부분취소", 10_000L);

        paymentFacade.cancel(userId, request);

        ArgumentCaptor<PaymentHistory> captor = ArgumentCaptor.forClass(PaymentHistory.class);
        verify(paymentHistoryRepository).save(captor.capture());

        assertEquals(PaymentEventType.PARTIAL_CANCEL, captor.getValue().getEventType());
        assertEquals(10_000L, captor.getValue().getAmount());
    }


    @Test
    @DisplayName("refund - 환불 시 REFUND 이력 남김")
    void refund_success() {
        Long userId = 1L;
        String orderIdStr = "3001";
        Long paidAmount = 60_000L;
        String paymentKey = "pay_key_refund";

        Order order = mock(Order.class);
        when(order.getOrderNumber()).thenReturn(3001L);

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
                30_000L   // 부분 환불
        );

        RefundResponse response = paymentFacade.refund(userId, request);

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
        FailRequest request = new FailRequest(
                "4001",
                10_000L,
                "NO_KEY",
                "ERROR_CODE",
                "에러 메시지"
        );

        paymentFacade.fail(request);

        ArgumentCaptor<PaymentHistory> captor = ArgumentCaptor.forClass(PaymentHistory.class);
        verify(paymentHistoryRepository).save(captor.capture());

        PaymentHistory history = captor.getValue();
        assertEquals(PaymentEventType.FAIL, history.getEventType());
        assertEquals(10_000, history.getAmount());
        assertTrue(history.getReason().contains("ERROR_CODE"));
    }

    @Test
    @DisplayName("confirm - EASY_PAY + methodDetail 있으면 응답이 TOSSPAY 같은 detail로 내려감")
    void confirm_easyPay_usesMethodDetail() {
        Long userId = 1L;
        String orderIdStr = "1001";
        Long amount = 50_000L;

        Order order = mock(Order.class);
        when(order.getOrderNumber()).thenReturn(1001L);
        when(order.getTotalPrice()).thenReturn(amount);
        when(orderRepository.findById(1001L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrder(order)).thenReturn(Optional.empty());

        when(paymentProvider.approve(any(PaymentProvider.ApproveCommand.class)))
                .thenReturn(new PaymentProvider.ApproveResult(
                        "TOSS",
                        "EASY_PAY",
                        "2025-12-01T12:00:00+09:00",
                        "TOSSPAY"
                ));

        ConfirmRequest request = new ConfirmRequest("TOSS", orderIdStr, "payKey", amount);

        ConfirmResponse response = paymentFacade.confirm(userId, request);

        assertEquals("PAID", response.status());
        assertEquals("TOSSPAY", response.method());
    }

    @Test
    @DisplayName("confirm - orderId에 -timestamp가 붙어도 앞 숫자로 주문 조회됨")
    void confirm_orderIdWithDash_normalized() {
        Long userId = 1L;
        String orderIdStr = "1001-20251226T120000";
        Long amount = 50_000L;

        Order order = mock(Order.class);
        when(order.getOrderNumber()).thenReturn(1001L);
        when(order.getTotalPrice()).thenReturn(amount);

        when(orderRepository.findById(1001L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrder(order)).thenReturn(Optional.empty());
        when(paymentProvider.approve(any())).thenReturn(
                new PaymentProvider.ApproveResult("TOSS", "CARD", "2025-12-01T12:00:00+09:00", null)
        );

        ConfirmRequest request = new ConfirmRequest("TOSS", orderIdStr, "payKey", amount);

        ConfirmResponse response = paymentFacade.confirm(userId, request);

        assertEquals("1001", response.orderId()); // orderNumber 기반 응답
    }

}
