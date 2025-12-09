package com.nhnacademy.order_payments.exception;

public class NotFoundDelivery extends RuntimeException {
    public NotFoundDelivery(String message) {
        super(message);
    }
}
