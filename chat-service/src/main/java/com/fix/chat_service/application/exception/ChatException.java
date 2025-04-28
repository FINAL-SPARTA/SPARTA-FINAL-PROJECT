package com.fix.chat_service.application.exception;

import org.springframework.http.HttpStatus;

import com.fix.common_service.exception.CustomException;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class ChatException extends CustomException {

	public ChatException(ChatErrorType errorType) {
		super(errorType.getCode(), errorType.getMessage(), errorType.getStatus());
	}

	@Getter
	public enum ChatErrorType {
		CHATROOM_NOT_FOUND("CHAT_001", HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."),
		CHATROOM_CANNOT_ACCESS("CHAT_002", HttpStatus.BAD_REQUEST, "채팅방 관련 권한이 없습니다.");

		private final String code;
		private final HttpStatus status;
		private final String message;

		ChatErrorType(String code, HttpStatus status, String message) {
			this.code = code;
			this.status = status;
			this.message = message;
		}
	}

}
