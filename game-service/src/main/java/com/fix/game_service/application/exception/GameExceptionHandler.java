package com.fix.game_service.application.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.fix.common_service.dto.CommonResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GameExceptionHandler {

	@ExceptionHandler(exception = {GameException.class})
	public ResponseEntity<CommonResponse<GameException>> errorResponse(
		GameException exception, HttpServletRequest request) {
		log.warn("GameException 발생 : URI={}, Method={}, ErrorCode={}, ErrorMessage={}",
			request.getRequestURI(), request.getMethod(),
			exception.getErrorCode(), exception.getMessage());

		return ResponseEntity.status(exception.getStatus()).body(CommonResponse.fail(
			exception.getErrorCode(), exception.getMessage(), exception.getStatus().value()
		));
	}

	@ExceptionHandler(exception = {IllegalArgumentException.class})
	public ResponseEntity<CommonResponse<String>> illegalArgumentException(
		IllegalArgumentException exception, HttpServletRequest request) {
		log.warn("IllegalArgumentException 발생 : URI={}, Method={}, ErrorMessage={}",
			request.getRequestURI(), request.getMethod(), exception.getMessage());

		return ResponseEntity.badRequest().body(CommonResponse.fail(
			"Illegal Argument", exception.getMessage(), 400
		));
	}

	@ExceptionHandler(exception = {Exception.class})
	public ResponseEntity<CommonResponse<String>> exception(
		Exception exception, HttpServletRequest request) {
		log.error("예상하지 못한 Exception 발생 : URI={}, Method={}, ErrorMessage={}",
			request.getRequestURI(), request.getMethod(), exception.getMessage());

		return ResponseEntity.internalServerError().body(CommonResponse.fail(
			"Internal Server Error", exception.getMessage(), 500
		));
	}
}
