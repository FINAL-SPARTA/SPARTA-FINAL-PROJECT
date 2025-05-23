package com.fix.user_service.application.exception;

import com.fix.common_service.dto.CommonResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String TRACE_ID = "traceId"; // MDC 키

    @ExceptionHandler(UserException.class)
    public ResponseEntity<CommonResponse<UserException>> handleUserException(
        UserException exception, HttpServletRequest request) {
        String traceId = MDC.get(TRACE_ID);

        log.warn("[{}] UserException 발생 : URI={}, Method={}, ErrorCode={}, ErrorMessage={}",
                traceId, request.getRequestURI(), request.getMethod(),
                exception.getErrorCode(), exception.getMessage());

        return ResponseEntity.status(exception.getStatus()).body(CommonResponse.fail(
                exception.getErrorCode(), exception.getMessage(), exception.getStatus().value()
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponse<String>> handleValidationException(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        String traceId = MDC.get(TRACE_ID);
        String errorMessages = exception.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));

        log.warn("[{}] ValidationException 발생 : URI={}, Method={}, ErrorMessages={}",
                traceId, request.getRequestURI(), request.getMethod(), errorMessages);

        return ResponseEntity.badRequest().body(CommonResponse.fail(
                "Validation Error", errorMessages, HttpStatus.BAD_REQUEST.value()
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CommonResponse<String>> handleIllegalArgumentException(
            IllegalArgumentException exception, HttpServletRequest request) {
        String traceId = MDC.get(TRACE_ID);

        log.warn("[{}] IllegalArgumentException 발생 : URI={}, Method={}, ErrorMessage={}",
                traceId, request.getRequestURI(), request.getMethod(), exception.getMessage());

        return ResponseEntity.badRequest().body(CommonResponse.fail(
                "Illegal Argument", exception.getMessage(), HttpStatus.BAD_REQUEST.value()
        ));
    }

    // 예상하지 못한 Exception 도 로깅
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<String>> handleGeneralException(
            Exception exception, HttpServletRequest request) {
        String traceId = MDC.get(TRACE_ID);

        log.error("[{}] 예상하지 못한 Exception 발생 : URI={}, Method={}, ErrorMessage={}",
                traceId, request.getRequestURI(), request.getMethod(), exception.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonResponse.fail(
                "Internal Server Error", exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()
        ));
    }
}
