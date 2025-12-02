package com.nhnacademy.order_payments.config;

import com.nhnacademy.order_payments.exception.BusinessException;
import com.nhnacademy.order_payments.exception.CartDetailNotFoundException;
import com.nhnacademy.order_payments.exception.ExternalServiceException;
import com.nhnacademy.order_payments.exception.NotFoundUserCartException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler({CartDetailNotFoundException.class, NotFoundUserCartException.class})
    public ResponseEntity<Void> handleBookNotFoundCartException(Exception e) {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<String> handleExternalServiceException(Exception e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(e.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleBiz(BusinessException e) {
        return Map.of(
                "timestamp", Instant.now().toString(),
                "status", 400,
                "error", e.getCode(),
                "message", e.getMessage()
        );
    }
}
