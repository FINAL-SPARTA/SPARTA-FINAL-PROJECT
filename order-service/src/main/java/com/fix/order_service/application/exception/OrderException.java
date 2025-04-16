package com.fix.order_service.application.exception;

import com.fix.common_service.exception.CustomException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class OrderException extends CustomException {

    public OrderException(OrderErrorType errorType) {
        super(errorType.getCode(), errorType.getMessage(), errorType.getStatus());
    }

    public enum OrderErrorType {
        ORDER_NOT_FOUND("ORDER_001", HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다"),
        TICKET_NOT_FOUND("ORDER_002", HttpStatus.NOT_FOUND, "티켓을 찾을 수 없습니다"),
        INVALID_ORDER_STATUS("ORDER_003", HttpStatus.BAD_REQUEST, "잘못된 주문 상태입니다"),
        ORDER_ROLE_HEADER_MISSING("ORDER_004", HttpStatus.UNAUTHORIZED, "사용자 역할 헤더가 누락되었습니다"),
        ORDER_ROLE_UNAUTHORIZED("ORDER_005", HttpStatus.FORBIDDEN, "해당 요청에 대한 권한이 없습니다"),
        INVALID_REQUEST("ORDER_006", HttpStatus.BAD_REQUEST, "잘못된 요청입니다");

        private final String code;
        private final HttpStatus status;
        private final String message;

        OrderErrorType(String code, HttpStatus status, String message) {
            this.code = code;
            this.status = status;
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public HttpStatus getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }
    }
}