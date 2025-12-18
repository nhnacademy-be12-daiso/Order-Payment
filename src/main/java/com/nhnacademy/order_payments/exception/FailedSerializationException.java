package com.nhnacademy.order_payments.exception;

import com.fasterxml.jackson.core.JsonProcessingException;

public class FailedSerializationException extends RuntimeException {
    public FailedSerializationException(String message, JsonProcessingException e) {
        super(message);
    }
}
