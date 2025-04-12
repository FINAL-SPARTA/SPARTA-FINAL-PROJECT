package com.fix.game_service.presentation.scheduler;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeactivateGameScheduler {

	private final RedisTemplate<String, String> redisTemplate;
	private final String ACTIVE_GAMES_KEY = "active-game";
	private final String QUEUE_KEY_PREFIX = "queue:";

	private final TaskScheduler taskScheduler;

	// 해당 경기가 active set에서 자동으로 삭제되도록 하는 메서드
	public void scheduleUnregister(UUID gameId, LocalDateTime closeDate) {
		Instant triggerTime = closeDate.atZone(ZoneId.systemDefault()).toInstant();

		taskScheduler.schedule(() -> {
			redisTemplate.opsForSet().remove(ACTIVE_GAMES_KEY, gameId.toString());
			redisTemplate.delete(QUEUE_KEY_PREFIX + gameId);
			log.info("대기열 큐 삭제: {}", QUEUE_KEY_PREFIX + gameId);
			log.info("자동 제거됨: {}", gameId);
		}, triggerTime);
	}
}
