package com.nhnacademy.order_payments.exception;

public class NotFoundUserCartException extends RuntimeException {
    public NotFoundUserCartException(Long userId) {
        super("해당 유저의 장바구니는 이미 비어있습니다 : " + userId.toString());
    }
}
