package com.fix.chat_service.application.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.fix.common_service.dto.CommonResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class ChatExceptionHandler {

	@ExceptionHandler(exception = {ChatException.class})
	public ResponseEntity<CommonResponse<ChatException>> errorResponse(ChatException exception) {
		log.error("[ErrorCode] = {} , [ErrorMessage] = {}", exception.getErrorCode(), exception.getMessage());

		return ResponseEntity.status(exception.getStatus()).body(CommonResponse.fail(
			exception.getErrorCode(), exception.getMessage(), exception.getStatus().value()
		));
	}

}