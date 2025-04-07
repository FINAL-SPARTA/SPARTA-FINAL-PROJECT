package com.fix.order_serivce.application.exception;

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
        INVALID_ORDER_STATUS("ORDER_003", HttpStatus.BAD_REQUEST, "잘못된 주문 상태입니다");

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