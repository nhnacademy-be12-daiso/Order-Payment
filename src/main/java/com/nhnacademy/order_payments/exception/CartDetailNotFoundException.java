package com.nhnacademy.order_payments.exception;

public class CartDetailNotFoundException extends RuntimeException {
    public CartDetailNotFoundException(Long bookId) {
        super("해당 책은 장바구니에 존재하지 않습니다 : " + String.valueOf(bookId));
    }
}
