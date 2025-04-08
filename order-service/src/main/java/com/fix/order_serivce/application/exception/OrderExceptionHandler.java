package com.fix.order_serivce.application.exception;

import com.fix.common_service.dto.CommonResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackages = "com.fix.order_serivce")
public class OrderExceptionHandler {

    @ExceptionHandler(OrderException.class)
    public ResponseEntity<CommonResponse<Void>> handleOrderException(OrderException exception) {
        log.error("[OrderError] code = {}, message = {}", exception.getErrorCode(), exception.getMessage());

        return ResponseEntity.status(exception.getStatus()).body(CommonResponse.fail(
                exception.getErrorCode(),
                exception.getMessage(),
                exception.getStatus().value()
        ));
    }
}
