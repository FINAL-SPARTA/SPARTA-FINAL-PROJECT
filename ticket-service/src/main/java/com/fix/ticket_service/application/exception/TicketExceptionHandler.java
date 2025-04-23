package com.fix.ticket_service.application.exception;

import com.fix.common_service.dto.CommonResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class TicketExceptionHandler {

    @ExceptionHandler(exception = {TicketException.class})
    public ResponseEntity<CommonResponse<TicketException>> errorResponse(
        TicketException exception, HttpServletRequest request) {
        log.warn("TicketException 발생 : URI={}, Method={}, ErrorCode={}, ErrorMessage={}",
            request.getRequestURI(), request.getMethod(),
            exception.getErrorCode(), exception.getMessage());

        return ResponseEntity.status(exception.getStatus()).body(CommonResponse.fail(
            exception.getErrorCode(), exception.getMessage(), exception.getStatus().value()
        ));
    }

    @ExceptionHandler(exception = {IllegalArgumentException.class})
    public ResponseEntity<CommonResponse<String>> illegalArgumentException(
        IllegalArgumentException exception, HttpServletRequest request) {
        log.warn("IllegalArgumentException 발생 : URI={}, Method={}, ErrorMessage={}",
            request.getRequestURI(), request.getMethod(), exception.getMessage());

        return ResponseEntity.badRequest().body(CommonResponse.fail(
            "Illegal Argument", exception.getMessage(), HttpStatus.BAD_REQUEST.value()
        ));
    }

    @ExceptionHandler(exception = {Exception.class})
    public ResponseEntity<CommonResponse<String>> exception(
        Exception exception, HttpServletRequest request) {
        log.error("예상하지 못한 Exception 발생 : URI={}, Method={}, ErrorMessage={}",
            request.getRequestURI(), request.getMethod(), exception.getMessage());

        return ResponseEntity.internalServerError().body(CommonResponse.fail(
            "Internal Server Error", exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()
        ));
    }
}
