package com.nhnacademy.order_payments.controller.payment;

import com.nhnacademy.order_payments.dto.payment.request.CancelRequest;
import com.nhnacademy.order_payments.dto.payment.request.ConfirmRequest;
import com.nhnacademy.order_payments.dto.payment.request.FailRequest;
import com.nhnacademy.order_payments.dto.payment.request.RefundRequest;
import com.nhnacademy.order_payments.dto.payment.response.CancelResponse;
import com.nhnacademy.order_payments.dto.payment.response.ConfirmResponse;
import com.nhnacademy.order_payments.dto.payment.response.PaymentHistoryResponse;
import com.nhnacademy.order_payments.dto.payment.response.RefundResponse;
import com.nhnacademy.order_payments.service.payment.PaymentFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/guest/payments")
@RequiredArgsConstructor
@Tag(name = "GuestPayments", description = "비회원 결제 승인/취소/환불/실패 API")
public class GuestPaymentController {

    private final PaymentFacade facade;

    @Operation(summary = "비회원 결제 승인 확인", description = "비회원 주문에 대해 paymentKey와 amount로 결제 승인 확인 후 주문/결제를 확정합니다.")
    @PostMapping("/confirm")
    public ConfirmResponse confirm(@Valid @RequestBody ConfirmRequest req) {
        // 비회원은 userId = null 로 전달
        return facade.confirm(null, req);
    }

    @Operation(summary = "비회원 결제 취소/부분취소", description = "paymentKey를 기준으로 취소/부분취소를 요청합니다.")
    @PostMapping("/cancel")
    public CancelResponse cancel(@Valid @RequestBody CancelRequest req) {
        return facade.cancel(null, req);
    }

    @Operation(summary = "비회원 결제 환불", description = "비회원 결제에 대해 환불 이력을 REFUND 상태로 기록합니다.")
    @PostMapping("/refund")
    public RefundResponse refund(@Valid @RequestBody RefundRequest req) {
        return facade.refund(null, req);
    }

    @Operation(summary = "비회원 결제 실패 기록", description = "토스 위젯에서 실패한 결제 정보를 저장합니다(FAIL 로그).")
    @PostMapping("/fail")
    public void fail(@Valid @RequestBody FailRequest req) {
        facade.fail(req);
    }

    @Operation(summary = "비회원 결제 히스토리 조회", description = "비회원 주문에 대한 결제 이력(APPROVE/CANCEL/REFUND/FAIL)을 조회합니다.")
    @GetMapping("/history/{orderIdOrNumber}")
    public List<PaymentHistoryResponse> getGuestHistory(@PathVariable("orderIdOrNumber") String orderIdOrNumber) {
        // 게스트는 userId 없음
        return facade.getHistory(null, orderIdOrNumber);
    }
}
