package com.nhnacademy.order_payments.exception;

public class NotFoundOrder extends RuntimeException {
    public NotFoundOrder(String message) {
        super(message);
    }
}
