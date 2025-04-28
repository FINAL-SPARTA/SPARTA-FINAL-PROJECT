package com.fix.game_service.presentation.scheduler;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.fix.game_service.application.exception.GameException;
import com.fix.game_service.application.service.QueueService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueScheduler {

	private final QueueService queueService;
	private final StringRedisTemplate redisTemplate;
	private final SimpMessagingTemplate messagingTemplate;
	private final String ACTIVE_GAMES_KEY = "active-game";
	// 한 번에 넘길 사용자 수
	private final int BATCH_SIZE = 10;

	@Scheduled(initialDelay = 5000, fixedDelay = 3000)
	public void processQueueLogic() {
		// 현재 예매 중인 경기 데이터 검색
		Set<String> gameIdObjects = redisTemplate.opsForSet().members(ACTIVE_GAMES_KEY);

		// 현재 예매 중인 경기가 없다면 반환
		if (gameIdObjects == null || gameIdObjects.isEmpty()) return;

		// 현재 예매 중인 경기 데이터가 있다면,
		for (Object obj : gameIdObjects) {
			// 경기 ID
			UUID gameId = UUID.fromString(obj.toString());
			String infoKey = ACTIVE_GAMES_KEY + gameId;

			// 경기 정보 탐색
			Map<Object, Object> gameInfo = redisTemplate.opsForHash().entries(infoKey);
			if (gameInfo.isEmpty()) continue;

			Set<String> allTokens = queueService.getAllTokensInQueue(gameId);
			if (allTokens != null && !allTokens.isEmpty()) {
				// 대기열 → 작업열
				Set<String> batchTokens = new HashSet<>(allTokens.stream().limit(BATCH_SIZE).collect(Collectors.toList()));
				sendToNextQueue(gameId, batchTokens);

				// 이외 토큰을 소유한 사용자에게 대기번호 전송
				Set<String> remainingTokens = allTokens.stream().skip(BATCH_SIZE).collect(Collectors.toSet());
				sendWaitingNumber(gameId, remainingTokens);

				// 작업열로 이동한 토큰 한 번에 삭제
				queueService.deleteTokensFromQueue(gameId, batchTokens);
			}
		}
	}

	/**
	 * 일정 사용자를 대기열에서 작업열로 전송
	 * @param gameId : 경기 ID
	 * @param tokens : 이동할 Tokens
	 */
	private void sendToNextQueue(UUID gameId, Set<String> tokens) {		
		for (String token : tokens) {
			log.info("다음 작업열로 이동할 Token: {}", token);
			try {
				queueService.moveToWorkingQueue(gameId, token);
			} catch (Exception e) {
				log.error("입장 처리 실패 [{}]: {}", token, e.getMessage());
			}
		}
	}

	/**
	 * 사용자에게 대기번호 전송
	 * @param gameId : 경기 ID
	 * @param allTokens : 대기열 전체 Token
	 */
	private void sendWaitingNumber(UUID gameId, Set<String> allTokens) {
		for (String token : allTokens) {
			Long waitNumber = queueService.getQueueNumber(gameId, token);
			String userToken = token.split("\\|")[0];
			messagingTemplate.convertAndSend("/topic/queue/status/" + gameId + "/" + userToken, waitNumber);
		}
	}

}