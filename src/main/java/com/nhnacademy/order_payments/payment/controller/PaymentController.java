package  com.nhnacademy.order_payments.payment.controller;

import com.nhnacademy.order_payments.dto.request.CancelRequest;
import com.nhnacademy.order_payments.dto.request.ConfirmRequest;
import com.nhnacademy.order_payments.dto.response.CancelResponse;
import com.nhnacademy.order_payments.dto.response.ConfirmResponse;
import com.nhnacademy.order_payments.payment.service.PaymentFacade;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.awt.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "결제 승인/취소 API")
public class PaymentController {
    private final PaymentFacade facade;

    @Operation(
            summary = "결제 승인 확인",
            description = "gateway/client로부터 받은 paymentKey와 amount로 PG 승인 확인 후 주문/결제를 확정합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "승인 완료",
                            content = @Content(schema = @Schema(implementation = ConfirmResponse.class))),
                    @ApiResponse(responseCode = "400", description = "검증 실패(금액 불일치, 주문없음 등)")
            }
    )

    @PostMapping("/confirm")
    public ConfirmResponse confirm(@Valid @RequestBody ConfirmRequest req) {
        return facade.confirm(req);
    }

    @Operation(
            summary = "결제 취소/부분취소",
            description = "paymentKey를 기준으로 취소/환불을 요청합니다. cancelAmount가 없으면 전액 취소합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "취소 완료",
                            content = @Content(schema = @Schema(implementation = CancelResponse.class))),
                    @ApiResponse(responseCode = "400", description = "검증 실패(취소 금액 오류, 결제내역 없음 등)")
            }
    )

    @PostMapping("/cancel")
    public CancelResponse cancel(@Valid @RequestBody CancelRequest req) {
        return facade.cancel(req);
    }
}




//package com.nhnacademy.order_payments.payment.controller;
//
//
//import com.nhnacademy.order_payments.dto.request.ConfirmRequest;
//import com.nhnacademy.order_payments.dto.response.ConfirmResponse;
//import com.nhnacademy.order_payments.payment.service.PaymentFacade;
//import jakarta.validation.Valid;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/api/payments")
//public class PaymentController {
//    private final PaymentFacade facade;
//    public PaymentController(PaymentFacade facade) {this.facade = facade;}
//
//    @PostMapping("/confirm")
//    public ConfirmResponse confirm(@Valid @RequestBody ConfirmRequest req) {
//        return facade.confirm(req);
//    }
//}