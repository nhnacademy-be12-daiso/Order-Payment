package com.nhnacademy.order_payments.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FailRequest {

    @NotBlank
    private String orderId; //주문번호

    @NotNull
    private Integer amount; //결제 시도 금액

    @NotBlank
    private String paymentKey;

    @NotBlank
    private String errorCode;

    @NotBlank
    private String errorMessage;
}
