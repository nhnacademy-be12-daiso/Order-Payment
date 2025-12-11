package com.nhnacademy.order_payments.controller;

import com.nhnacademy.order_payments.dto.request.CancelRequest;
import com.nhnacademy.order_payments.dto.request.ConfirmRequest;
import com.nhnacademy.order_payments.dto.request.FailRequest;
import com.nhnacademy.order_payments.dto.request.RefundRequest;
import com.nhnacademy.order_payments.dto.response.CancelResponse;
import com.nhnacademy.order_payments.dto.response.ConfirmResponse;
import com.nhnacademy.order_payments.dto.response.RefundResponse;
import com.nhnacademy.order_payments.controller.payment.PaymentController;
import com.nhnacademy.order_payments.service.payment.PaymentFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;   // 테스트에서 시큐리티 필터 끄기
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)   // ✅ 시큐리티 필터 제거
class PaymentControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    PaymentFacade paymentFacade;

    @Test
    @DisplayName("POST /api/payments/confirm - 정상 요청 시 200 OK + 응답 JSON")
    void confirm() throws Exception {
        ConfirmResponse response = new ConfirmResponse(
                "1001",
                "PAID",
                OffsetDateTime.parse("2025-12-01T12:00:00+09:00"),
                "CARD"
        );

        given(paymentFacade.confirm(eq(1L), any(ConfirmRequest.class)))
                .willReturn(response);

        String json = """
                {
                  "provider": "TOSS",
                  "orderId": "1001",
                  "paymentKey": "pay_123",
                  "amount": 50000
                }
                """;

        mockMvc.perform(post("/api/payments/confirm")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("1001"))
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.method").value("CARD"));
    }

    @Test
    @DisplayName("POST /api/payments/cancel - 정상 요청 시 200 OK")
    void cancel() throws Exception {
        CancelResponse response = new CancelResponse(
                "2001",
                "CANCELED",
                "2025-12-02T10:00:00+09:00",
                "CARD"
        );

        given(paymentFacade.cancel(eq(1L), any(CancelRequest.class)))
                .willReturn(response);

        String json = """
                {
                  "orderId": "2001",
                  "paymentKey": "pay_cancel",
                  "reason": "단순 변심",
                  "cancelAmount": 30000
                }
                """;

        mockMvc.perform(post("/api/payments/cancel")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("2001"))
                .andExpect(jsonPath("$.status").value("CANCELED"));
    }

    @Test
    @DisplayName("POST /api/payments/refund - 정상 요청 시 200 OK")
    void refund() throws Exception {
        RefundResponse response = new RefundResponse(
                "3001",
                "REFUNDED",
                "2025-12-03T10:00:00+09:00",
                "CARD"
        );

        given(paymentFacade.refund(eq(1L), any(RefundRequest.class)))
                .willReturn(response);

        String json = """
                {
                  "orderId": "3001",
                  "paymentKey": "pay_refund",
                  "reason": "상품 문제",
                  "cancelAmount": 20000
                }
                """;

        mockMvc.perform(post("/api/payments/refund")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("3001"))
                .andExpect(jsonPath("$.status").value("REFUNDED"));
    }

    @Test
    @DisplayName("POST /api/payments/fail - 200 OK, facade.fail() 호출 여부 확인")
    void fail() throws Exception {
        String json = """
                {
                  "orderId": "4001",
                  "amount": 10000,
                  "paymentKey": "no_key",
                  "errorCode": "ERROR",
                  "errorMessage": "에러 발생"
                }
                """;

        mockMvc.perform(post("/api/payments/fail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(paymentFacade).fail(any(FailRequest.class));
    }
}
