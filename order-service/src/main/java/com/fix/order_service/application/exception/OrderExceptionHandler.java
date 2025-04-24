package com.fix.order_service.application.exception;

import com.fix.common_service.dto.CommonResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackages = "com.fix.order_service")
public class OrderExceptionHandler {

    @ExceptionHandler(OrderException.class)
    public ResponseEntity<CommonResponse<Void>> handleOrderException(
        OrderException exception, HttpServletRequest request) {
        log.warn("OrderException 발생 : URI={}, Method={}, ErrorCode={}, ErrorMessage={}",
            request.getRequestURI(), request.getMethod(),
            exception.getErrorCode(), exception.getMessage());

        return ResponseEntity.status(exception.getStatus()).body(CommonResponse.fail(
                exception.getErrorCode(),
                exception.getMessage(),
                exception.getStatus().value()
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CommonResponse<Void>> handleIllegalArgumentException(
        IllegalArgumentException exception, HttpServletRequest request) {
        log.warn("IllegalArgumentException 발생 : URI={}, Method={}, ErrorMessage={}",
            request.getRequestURI(), request.getMethod(), exception.getMessage());

        return ResponseEntity.badRequest().body(CommonResponse.fail(
                "Illegal Argument",
                exception.getMessage(),
                400
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<Void>> handleException(
        Exception exception, HttpServletRequest request) {
        log.error("예상하지 못한 Exception 발생 : URI={}, Method={}, ErrorMessage={}",
            request.getRequestURI(), request.getMethod(), exception.getMessage());

        return ResponseEntity.internalServerError().body(CommonResponse.fail(
                "Internal Server Error",
                exception.getMessage(),
                500
        ));
    }
}
