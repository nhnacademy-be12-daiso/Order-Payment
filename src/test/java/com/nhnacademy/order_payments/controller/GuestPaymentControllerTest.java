package com.nhnacademy.order_payments.controller;

import com.nhnacademy.order_payments.controller.payment.GuestPaymentController;
import com.nhnacademy.order_payments.dto.payment.request.CancelRequest;
import com.nhnacademy.order_payments.dto.payment.request.ConfirmRequest;
import com.nhnacademy.order_payments.dto.payment.request.FailRequest;
import com.nhnacademy.order_payments.dto.payment.request.RefundRequest;
import com.nhnacademy.order_payments.dto.payment.response.CancelResponse;
import com.nhnacademy.order_payments.dto.payment.response.ConfirmResponse;
import com.nhnacademy.order_payments.dto.payment.response.PaymentHistoryResponse;
import com.nhnacademy.order_payments.dto.payment.response.RefundResponse;
import com.nhnacademy.order_payments.model.PaymentEventType;
import com.nhnacademy.order_payments.service.payment.PaymentFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// 비회원 결제 api 컨트롤러 연결 테스트
// 회원과 동일한 기능을 x-user-id 없이 수행되는지 확인
@WebMvcTest(GuestPaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
public class GuestPaymentControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    PaymentFacade paymentFacade;

    @MockitoBean(name = "jpaMappingContext")
    JpaMetamodelMappingContext jpaMappingContext;

    @Test
    @DisplayName("POST /api/guest/payments/confirm - 비회원 결제 승인")
    void confirm_guest() throws Exception {
        ConfirmResponse response = new ConfirmResponse(
                "1001",
                "PAID",
                OffsetDateTime.parse("2025-12-01T12:00:00+09:00"),
                "CARD"
        );

        given(paymentFacade.confirm(isNull(), any(ConfirmRequest.class)))
                .willReturn(response);

        String json = """
                {
                  "provider": "TOSS",
                  "orderId": "1001",
                  "paymentKey": "pay_123",
                  "amount": 50000
                }
                """;

        mockMvc.perform(post("/api/guest/payments/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("1001"))
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.method").value("CARD"));

        verify(paymentFacade).confirm(isNull(), any(ConfirmRequest.class));
    }

    @Test
    @DisplayName("POST /api/guest/payments/cancel - 비회원 결제 취소")
    void cancel_guest() throws Exception {
        CancelResponse response = new CancelResponse(
                "2001",
                "CANCELED",
                "2025-12-02T10:00:00+09:00",
                "CARD"
        );

        given(paymentFacade.cancel(isNull(), any(CancelRequest.class)))
                .willReturn(response);

        String json = """
                {
                  "orderId": "2001",
                  "paymentKey": "pay_cancel",
                  "reason": "단순 변심",
                  "cancelAmount": 30000
                }
                """;

        mockMvc.perform(post("/api/guest/payments/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("2001"))
                .andExpect(jsonPath("$.status").value("CANCELED"));

        verify(paymentFacade).cancel(isNull(), any(CancelRequest.class));
    }

    @Test
    @DisplayName("POST /api/guest/payments/refund - 비회원 결제 환불")
    void refund_guest() throws Exception {
        RefundResponse response = new RefundResponse(
                "3001",
                "REFUNDED",
                "2025-12-03T10:00:00+09:00",
                "CARD"
        );

        given(paymentFacade.refund(isNull(), any(RefundRequest.class)))
                .willReturn(response);

        String json = """
                {
                  "orderId": "3001",
                  "paymentKey": "pay_refund",
                  "reason": "상품 문제",
                  "cancelAmount": 20000
                }
                """;

        mockMvc.perform(post("/api/guest/payments/refund")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("3001"))
                .andExpect(jsonPath("$.status").value("REFUNDED"));

        verify(paymentFacade).refund(isNull(), any(RefundRequest.class));
    }

    @Test
    @DisplayName("POST /api/guest/payments/fail - 비회원 결제 실패 기록")
    void fail_guest() throws Exception {
        String json = """
                {
                  "orderId": "4001",
                  "amount": 10000,
                  "paymentKey": "no_key",
                  "errorCode": "ERROR",
                  "errorMessage": "에러 발생"
                }
                """;

        mockMvc.perform(post("/api/guest/payments/fail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(paymentFacade).fail(any(FailRequest.class));
    }

    @Test
    @DisplayName("GET /api/guest/payments/history/{orderIdOrNumber} - 비회원 결제 히스토리 조회")
    void history_guest() throws Exception {
        List<PaymentHistoryResponse> responses = List.of(
                new PaymentHistoryResponse(
                        PaymentEventType.APPROVE,
                        50000L,
                        null,
                        LocalDateTime.of(2025, 12, 1, 12, 0),
                        "CARD"
                )
        );

        given(paymentFacade.getHistory(isNull(), eq("1001")))
                .willReturn(responses);

        mockMvc.perform(get("/api/guest/payments/history/{orderIdOrNumber}", "1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventType").value("APPROVE"))
                .andExpect(jsonPath("$[0].amount").value(50000))
                .andExpect(jsonPath("$[0].method").value("CARD"));

        verify(paymentFacade).getHistory(isNull(), eq("1001"));
    }
}
