package com.fix.game_service.presentation.controller;

import com.fix.common_service.aop.ApiLogging;
import com.fix.game_service.application.service.QueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/games")
public class QueueController {

	private final QueueService queueService;

	@ApiLogging
	@PostMapping("/{gameId}/enter")
	public ResponseEntity<Map<String, Object>> enterQueue(@PathVariable UUID gameId, @RequestHeader("x-user-id") Long userId) {
		Map<String, Object> response = queueService.enterQueue(gameId, userId);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{gameId}/status/{token}")
	public ResponseEntity<Long> getWaitNumber(@PathVariable UUID gameId,
		@RequestHeader("x-user-id") Long userId,
		@PathVariable String token) {
		// 1. 사용자가 작업열에 있는가 ?
		Boolean isInWorkingQueue = queueService.isInWorkingQueue(gameId, userId, token);
		if (isInWorkingQueue) {
			// 다음 API 호출 후 보내주기
			log.info("token - {}: 다음 화면으로 넘어갑니다", token);
		}
		
		// 2. 작업열에 없다면 대기열에 있는지 파악
		Long waitNumber = queueService.getQueueNumber(gameId, token);

		// 3. 대기열에 있으면 대기 번호 발급
		if (waitNumber != null) {
			return ResponseEntity.ok(waitNumber);
		// 4. 대기열에도 없다면 오류
		} else {
			return ResponseEntity.badRequest().body(0L);
		}
	}

	@DeleteMapping("/{gameId}/leave/{token}")
	public ResponseEntity<Void> leaveQueue(@PathVariable UUID gameId,
		@RequestHeader("x-user-id") Long userId,
		@PathVariable String token) {
		queueService.leaveQueueByRequest(gameId, userId, token);
		return ResponseEntity.ok().build();
	}

}
