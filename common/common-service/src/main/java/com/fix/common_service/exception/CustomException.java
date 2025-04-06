package com.fix.common_service.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public class CustomException extends RuntimeException {

    private final String errorCode;
    private final String message;
    private final HttpStatus status;

    // ✅ getCode(), getMessage(), getStatus() 메서드 추가
    public String getCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
