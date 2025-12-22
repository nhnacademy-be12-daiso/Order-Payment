package com.nhnacademy.order_payments.config;

import com.nhnacademy.order_payments.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler({CartDetailNotFoundException.class, NotFoundUserCartException.class, NotFoundOrderException.class})
    public ResponseEntity<Void> handleBookNotFoundCartException(Exception e) {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler({ExternalServiceException.class, FailedSerializationException.class})
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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllException(Exception e) {
        log.error("Unhandled exception occurred: ", e);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", 500,
                "error", "INTERNAL_SERVER_ERROR",
                "message", e.toString()
        ));
    }
}
