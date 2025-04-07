package com.fix.game_service.application.exception;

import org.springframework.http.HttpStatus;

import com.fix.common_service.exception.CustomException;

import lombok.Getter;

@Getter
public class GameException extends CustomException {

	public GameException(GameErrorType errorType) {
		super(errorType.getCode(), errorType.getMessage(), errorType.getStatus());
	}

	public enum GameErrorType {
		GAME_NOT_FOUND("GAME_001", HttpStatus.NOT_FOUND, "경기를 찾을 수 없습니다"),
		GAME_ROLE_UNAUTHORIZED("GAME_002", HttpStatus.UNAUTHORIZED, "경기 관련 권한이 없습니다");

		private final String code;
		private final HttpStatus status;
		private final String message;

		GameErrorType(String code, HttpStatus status, String message) {
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
