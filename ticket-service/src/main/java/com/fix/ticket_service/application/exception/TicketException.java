package com.fix.ticket_service.application.exception;

import com.fix.common_service.exception.CustomException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class TicketException extends CustomException {

    public TicketException(TicketErrorType errorType) {
        super(errorType.getCode(), errorType.getMessage(), errorType.getStatus());
    }

    @Getter
    public enum TicketErrorType {
        SEAT_LOCK_ACQUIRE_FAILED("TICKET_001", HttpStatus.BAD_REQUEST, "다른 사용자가 좌석을 선택 중입니다."),
        SEAT_ALREADY_RESERVED_OR_SOLD("TICKET_002", HttpStatus.BAD_REQUEST, "이미 예약되었거나 판매된 좌석이 포함되어 있습니다."),
        SEAT_LOCK_INTERRUPTED("TICKET_003", HttpStatus.INTERNAL_SERVER_ERROR, "락을 획득하는 동안 문제가 발생했습니다."),
        TICKET_NOT_FOUND("TICKET_004", HttpStatus.NOT_FOUND, "티켓을 찾을 수 없습니다."),
        TICKET_CANNOT_DELETE("TICKET_005", HttpStatus.BAD_REQUEST, "삭제할 수 있는 티켓이 없습니다."),
        QUEUE_TOKEN_REQUIRED("TICKET_006", HttpStatus.BAD_REQUEST, "큐 토큰이 필요합니다."),
        QUEUE_TOKEN_INVALID("TICKET_007", HttpStatus.BAD_REQUEST, "큐 토큰이 유효하지 않습니다."),
        UNAUTHORIZED_ACCESS("TICKET_008", HttpStatus.UNAUTHORIZED, "조회, 수정 권한이 없습니다.");

        private final String code;
        private final HttpStatus status;
        private final String message;

        TicketErrorType(String code, HttpStatus status, String message) {
            this.code = code;
            this.status = status;
            this.message = message;
        }
    }
}
