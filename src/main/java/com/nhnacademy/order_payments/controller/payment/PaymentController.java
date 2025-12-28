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
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "결제 승인/취소/환불/실패 API")
public class PaymentController {

    private final PaymentFacade facade;

    @Operation(summary = "결제 승인 확인", description = "paymentKey와 amount로 결제 승인 확인 후 주문/결제를 확정합니다.")
    @PostMapping("/confirm")
    public ConfirmResponse confirm(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                   @Valid @RequestBody ConfirmRequest req) {
        return facade.confirm(userId, req);
    }

    @Operation(summary = "결제 취소/부분취소", description = "paymentKey를 기준으로 취소/부분취소를 요청합니다. cancelAmount가 없으면 전액 취소합니다.")
    @PostMapping("/cancel")
    public CancelResponse cancel(@RequestHeader("X-User-Id") Long userId,
                                 @Valid @RequestBody CancelRequest req) {
        return facade.cancel(userId, req);
    }

    @Operation(summary = "결제 환불", description = "환불 이력을 REFUND 상태로 기록합니다.")
    @PostMapping("/refund")
    public RefundResponse refund(@RequestHeader("X-User-Id") Long userId,
                                 @Valid @RequestBody RefundRequest req) {
        return facade.refund(userId, req);
    }

    @Operation(summary = "결제 실패 기록", description = "토스 위젯에서 실패한 결제 정보를 저장합니다(FAIL 로그).")
    @PostMapping("/fail")
    public void fail(@Valid @RequestBody FailRequest req) {
        facade.fail(req);
    }

    @Operation(summary = "결제 히스토리 조회", description = "특정 주문에 대한 APPROVE / CANCEL / REFUND / FAIL 등의 이력을 조회합니다.")
    @GetMapping("/history/{orderIdOrNumber}")
    public List<PaymentHistoryResponse> getHistory(@RequestHeader("X-User-Id") Long userId,
                                                   @PathVariable("orderIdOrNumber") String orderIdOrNumber) {
        return facade.getHistory(userId, orderIdOrNumber);
    }
}
