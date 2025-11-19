package com.nhnacademy.order_payments.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record ConfirmRequest(
        @Schema(description = "PG 제공자", example = "Fake or Toss")
        String provider,
        @Schema(description = "주문 식별자", example = "1001")
        String orderId,
        @Schema(description = "PG가 발급한 결제 키", example = "fake_key_001")
        String paymentKey,
        @Schema(description = "승인 금액(원)", example = "50000")
        Integer amount,
        @Schema(description = "사용 포인트(원)", example = "0")
        Integer usedPoint
) {}



//package com.nhnacademy.order_payments.dto;
//
//import jakarta.validation.constraints.NotBlank;
//import jakarta.validation.constraints.Positive;
//import jakarta.validation.constraints.PositiveOrZero;
//
////결제 승인 요청 dto
//public record ConfirmRequest(
//        @NotBlank String provider, //"Fake" || "Toss"
//        @NotBlank String orderId, //주문식별자
//        @NotBlank String paymentKey, //PG에서 받은 키
//        @Positive Integer amount, //실 결제금
//        @PositiveOrZero Integer usedPoint //사용한 포인트 금액
//) {
//}
