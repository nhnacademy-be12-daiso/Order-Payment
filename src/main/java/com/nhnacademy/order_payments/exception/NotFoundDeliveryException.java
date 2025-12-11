package com.nhnacademy.order_payments.exception;

public class NotFoundDeliveryException extends RuntimeException {
    public NotFoundDeliveryException(String message) {
        super(message);
    }
}
