//package com.nhnacademy.order_payments.exception;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.ResponseStatus;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//
//import java.time.Instant;
//import java.util.Map;
//
//@RestControllerAdvice
//public class GlobalExceptionHandler {
//    @ExceptionHandler(BusinessException.class)
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    public Map<String,Object> handleBiz(BusinessException e) {
//        return Map.of(
//                "timestamp", Instant.now().toString(),
//                "status", 400,
//                "error", e.getCode(),
//                "message", e.getMessage()
//        );
//    }
//}
