package com.nhnacademy.order_payments.config;

import com.nhnacademy.order_payments.exception.CartDetailNotFoundException;
import com.nhnacademy.order_payments.exception.ExternalServiceException;
import com.nhnacademy.order_payments.exception.NotFoundUserCartException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.badRequest().build();
        // ----> 로그 찍어놓으니까 괜찮겠지?
    }
    @ExceptionHandler({CartDetailNotFoundException.class, NotFoundUserCartException.class})
    public ResponseEntity<Void> handleBookNotFoundCartException(Exception e) {
        return ResponseEntity.notFound().build();
    }
    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<String> handleExternalServiceException(Exception e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(e.getMessage());
    }



}
