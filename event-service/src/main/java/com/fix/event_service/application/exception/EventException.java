package com.fix.event_service.application.exception;

import com.fix.common_service.exception.CustomException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class EventException extends CustomException {

	public EventException(EventErrorType errorType) {
		super(errorType.getCode(), errorType.getMessage(), errorType.getStatus());
	}

	@Getter
	public enum EventErrorType {
		EVENT_NOT_FOUND("EVENT_001", HttpStatus.NOT_FOUND, "이벤트를 찾을 수 없습니다."),
		EVENT_CANNOT_UPDATE("EVENT_002", HttpStatus.BAD_REQUEST, "진행중이거나 종료된 이벤트는 수정할 수 없습니다."),
		EVENT_CANNOT_DELETE("EVENT_003", HttpStatus.BAD_REQUEST, "응모가 진행중인 이벤트는 삭제할 수 없습니다."),
		EVENT_NOT_OPEN_FOR_APPLY("EVENT_004", HttpStatus.BAD_REQUEST, "이벤트 응모 기간이 아닙니다."),
		EVENT_INVALID_PERIOD("EVENT_005", HttpStatus.BAD_REQUEST, "이벤트 시간 범위가 유효하지 않습니다."),
		REWARD_LACK("EVENT_006", HttpStatus.BAD_REQUEST, "상품 재고가 부족합니다."),
		EVENT_ROLE_UNAUTHORIZED("EVENT_007", HttpStatus.UNAUTHORIZED, "이벤트 관련 권한이 없습니다.");

		private final String code;
		private final HttpStatus status;
		private final String message;

		EventErrorType(String code, HttpStatus status, String message) {
			this.code = code;
			this.status = status;
			this.message = message;
		}
	}
}
