package com.fix.user_service.application.exception;

import com.fix.common_service.exception.CustomException;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import java.util.Map;
@Getter
public class UserException extends CustomException {

    public UserException(UserErrorType errorType) {
        super(errorType.getCode(), errorType.getMessage(), errorType.getStatus());
    }

//    ✅ 예외 응답을 Map<String, Object> 형식으로 바로 꺼낼 수 있게 추가
    public Map<String, Object> getErrorResponse() {
        return Map.of(
            "code", getCode(),
            "message", getMessage(),
            "status", getStatus().value()
        );
    }

    @Getter
    public static class ErrorInfo {
        private final String code;
        private final String message;
        private final int status;

        public ErrorInfo(UserException ex) {
            this.code = ex.getCode();
            this.message = ex.getMessage();
            this.status = ex.getStatus().value();
        }
    }//    ✅ 응답용 내부 클래스 (선택)
    public enum UserErrorType {
        USER_NOT_FOUND("USER_001", HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
        DUPLICATE_USERNAME("USER_002", HttpStatus.CONFLICT, "이미 사용 중인 사용자명입니다."),
        DUPLICATE_EMAIL("USER_003", HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
        INVALID_ROLE("USER_004", HttpStatus.BAD_REQUEST, "잘못된 사용자 역할입니다."),
        PERMISSION_DENIED("USER_005", HttpStatus.FORBIDDEN, "권한이 없습니다."),
        INTERNAL_SERVER_ERROR("USER_006", HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
        DATABASE_ERROR("USER_007", HttpStatus.INTERNAL_SERVER_ERROR, "데이터베이스 처리 중 오류가 발생했습니다."),
        AUTHENTICATION_FAILED("USER_008", HttpStatus.UNAUTHORIZED, "인증에 실패했습니다."),
        TOKEN_EXPIRED("USER_009", HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
        USER_SERVICE_UNAVAILABLE("USER_503", HttpStatus.SERVICE_UNAVAILABLE, "User 서비스에 접근할 수 없습니다."),
        INVALID_UUID_FORMAT("USER_010", HttpStatus.BAD_REQUEST, "잘못된 UUID 형식입니다."),
        NOT_ENOUGH_POINTS("USER_011", HttpStatus.BAD_REQUEST, "포인트가 부족합니다."),
        DUPLICATE_PHONE_NUMBER("USER_012", HttpStatus.CONFLICT, "이미 사용 중인 전화번호입니다.");

        private final String code;
        private final HttpStatus status;
        private final String message;

        UserErrorType(String code, HttpStatus status, String message) {
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
