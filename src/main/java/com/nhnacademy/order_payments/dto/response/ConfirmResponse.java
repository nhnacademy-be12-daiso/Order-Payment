package com.nhnacademy.order_payments.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

public record ConfirmResponse(
        @Schema(description = "주문 식별자", example = "1001")
        String orderId,
        @Schema(description = "결제 상태", example = "PAID")
        String status,
        @Schema(description = "승인 시각")
        OffsetDateTime approvedAt,
        @Schema(description = "결제 수단", example = "CARD")
        String method
) {}



//package com.nhnacademy.order_payments.dto;
//
////결제 승인 응답 dto
//public record ConfirmResponse(
//        String orderId, //주문번호
//        String status, //상태값(ex.결제완료)
//        String approvedAt, //결제 승인 시각(로컬시 now, 토스사용시 토스가 내려준 승인 시각)
//        String method //결제수단
//) {
//}
