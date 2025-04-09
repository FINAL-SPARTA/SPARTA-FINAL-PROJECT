package com.fix.event_service.application.exception;

import com.fix.common_service.dto.CommonResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class EventExceptionHandler {

	@ExceptionHandler(exception = {EventException.class})
	public ResponseEntity<CommonResponse<EventException>> errorResponse(EventException exception) {
		log.error("[ErrorCode] = {} , [ErrorMessage] = {}", exception.getErrorCode(), exception.getMessage());

		return ResponseEntity.status(exception.getStatus()).body(CommonResponse.fail(
			exception.getErrorCode(), exception.getMessage(), exception.getStatus().value()
		));
	}
}
