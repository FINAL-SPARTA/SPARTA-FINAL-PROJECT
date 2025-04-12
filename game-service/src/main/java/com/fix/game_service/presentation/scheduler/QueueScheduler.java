package com.fix.game_service.presentation.scheduler;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fix.game_service.application.service.QueueService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueScheduler {

	private final QueueService queueService;
	private final StringRedisTemplate redisTemplate;
	private final String ACTIVE_GAMES_KEY = "active-game";

	// 한 번에 넘길 사용자 수
	private final int BATCH_SIZE = 10;

	@Scheduled(initialDelay = 5000, fixedRate = 3000)
	public void processQueueLogic() {
		Set<String> gameIdObjects = redisTemplate.opsForSet().members(ACTIVE_GAMES_KEY);

		// 현재 예매중인 경기가 없다면 반환
		if (gameIdObjects == null || gameIdObjects.isEmpty()) return;

		// 데이터가 있다면,
		for (Object obj : gameIdObjects) {
			UUID gameId = UUID.fromString(obj.toString());
			String infoKey = ACTIVE_GAMES_KEY + gameId;

			Map<Object, Object> gameInfo = redisTemplate.opsForHash().entries(infoKey);
			if (gameInfo.isEmpty()) continue;

			// 정상 처리: 대기열 → 작업열
			Set<String> tokens = queueService.getRemainUsersInQueue(gameId, BATCH_SIZE);
			if (tokens == null || tokens.isEmpty()) continue;

			for (String token : tokens) {
				log.info("다음 작업열로 이동할 Token: {}", token);
				try {
					queueService.moveToWorkingQueue(gameId, token);
					queueService.leaveQueue(gameId, token);
				} catch (Exception e) {
					log.error("입장 처리 실패 [{}]: {}", token, e.getMessage());
				}
			}
		}
	}


}
