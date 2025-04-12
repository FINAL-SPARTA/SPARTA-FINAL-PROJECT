package com.fix.stadium_service.application.exception;

import com.fix.common_service.exception.CustomException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class StadiumException extends CustomException {

	public StadiumException(StadiumErrorType errorType) {
		super(errorType.getCode(), errorType.getMessage(), errorType.getStatus());
	}


    @Getter
	public enum StadiumErrorType {
		STADIUM_NOT_FOUND("STADIUM_001", HttpStatus.NOT_FOUND, "경기장을 찾을 수 없습니다"),
		STADIUM_NAME_NOT_FOUND("STADIUM_002", HttpStatus.NOT_FOUND, "해당 팀의 경기장이 존재하지 않습니다."),
		STADIUM_ROLE_UNAUTHORIZED("STADIUM_003", HttpStatus.UNAUTHORIZED, "경기장 관련 권한이 없습니다"),
		SEAT_SECTION_NOT_FOUND("STADIUM_004",HttpStatus.NOT_FOUND,"해당 섹션을 찾을 수 없습니다."),
		SEAT_NOT_AVAILABLE("STADIUM_005",HttpStatus.BAD_REQUEST,"해당 좌석은 이용할 수 없습니다"),
		STADIUM_DUPLICATE("STADIUM_006",HttpStatus.BAD_REQUEST,"해당 팀은 이미 존재합니다.");

		private final String code;
		private final HttpStatus status;
		private final String message;

		StadiumErrorType(String code, HttpStatus status, String message) {
			this.code = code;
			this.status = status;
			this.message = message;
		}

    }
}
