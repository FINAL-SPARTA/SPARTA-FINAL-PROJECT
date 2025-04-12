package com.fix.game_service.application.service;

import com.fix.game_service.application.exception.GameException;
import com.fix.game_service.domain.model.Game;
import com.fix.game_service.domain.repository.GameRepository;
import com.fix.game_service.presentation.scheduler.DeactivateGameScheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.fix.game_service.application.exception.GameException.GameErrorType.GAME_CANNOT_RESERVATION;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueService {

	private final GameRepository gameRepository;
	private final RedisTemplate<String, String> redisTemplate;
	private final DeactivateGameScheduler deactivateGameScheduler;
	private final String QUEUE_KEY_PREFIX = "queue:";
	private final String WORKING_QUEUE_KEY_PREFIX = "queue:working:";
	private final String ACTIVE_GAMES_KEY = "active-game";
	private final Long MAX_WORKING_QUEUE_SIZE = 50L;
	private final int FREE_PASS_LIMIT = 10;

	public Map<String, Object> enterQueue(UUID gameId, Long userId) {
		// 1. 해당 경기를 찾아서 예매 가능 시간인지 확인
		Game game = findGame(gameId);
		if (game.getOpenDate().isAfter(LocalDateTime.now()) ||
			game.getCloseDate().isBefore(LocalDateTime.now())) {
			throw new GameException(GAME_CANNOT_RESERVATION);
		}

		// 2. 입장한 사용자에게 Token 발급
		String queueKey = QUEUE_KEY_PREFIX + gameId;
		String token = UUID.randomUUID().toString();

		// 3. Map에 저장
		Map<String, Object> response = new HashMap<>();
		response.put("token", token);

		// 4. 발급한 토큰 Redis에 추가 (대기열)
		long timestamp = Instant.now().toEpochMilli();
		redisTemplate.opsForZSet().add(queueKey, token, timestamp);

		// 5. Sorted Set에 추가된 후 순위(대기번호)를 조회하여 반환
		Long rank = redisTemplate.opsForZSet().rank(queueKey, token);

		// 6. 현재 활성화된 경기 추가
		registerActiveGame(gameId, game.getStadiumId(), game.getOpenDate(), game.getCloseDate());

		// 7. 대기열에서의 순위 반환
		response.put("position", rank);
		log.info("token: {}, rank : {}, userId: {} ", token, rank, userId);
		return response;
	}

	/**
	 * 현재 활성화된 경기 추가
	 * @param gameId : 경기 ID
	 * @param stadiumId : 경기장 ID
	 * @param openDate : 경기 예매 오픈 날짜
	 * @param closeDate : 경기 예매 마감 날짜
	 */
	private void registerActiveGame(UUID gameId, Long stadiumId, LocalDateTime openDate, LocalDateTime closeDate) {
		redisTemplate.opsForSet().add(ACTIVE_GAMES_KEY, gameId.toString());

		Map<String, String> gameInfo = new HashMap<>();
		gameInfo.put("stadiumId", stadiumId.toString());
		gameInfo.put("openDate", openDate.toString());
		gameInfo.put("closeDate", closeDate.toString());
		redisTemplate.opsForHash().putAll(ACTIVE_GAMES_KEY + gameId, gameInfo);

		// 등록 시 스케줄링 (closeDate에 맞춰서 활성화 경기 Set에서 경기 제거)
		deactivateGameScheduler.scheduleUnregister(gameId, closeDate);
	}

	/**
	 * 현재 사용자의 대기 순서를 반환
	 * @param gameId : 경기 ID
	 * @param token : 대기열 token
	 * @return : token에 해당하는 순서 반환
	 */
	public Long getQueueNumber(UUID gameId, String token) {
		String queueKey = QUEUE_KEY_PREFIX + gameId;
		Long rank = redisTemplate.opsForZSet().rank(queueKey, token);
		return rank;
	}

	/**
	 * 대기열을 떠나는 경우
	 * @param gameId : 경기 ID
	 * @param token : 떠나는 사용자의 토큰
	 */
	public void leaveQueue(UUID gameId, String token) {
		String queueKey = "queue:" + gameId;
		redisTemplate.opsForZSet().remove(queueKey, token);
	}

	/**
	 * 대기열을 거치지 않아도 되는가 ?
	 * @param gameId : 경기 ID
	 * @return : 대기열을 거치지 않아도 되는지 여부 반환
	 */
	public boolean canSkipQueue(UUID gameId) {
		String queueKey = QUEUE_KEY_PREFIX + gameId;
		Long queueSize = redisTemplate.opsForZSet().size(queueKey);
		return queueSize != null && queueSize < FREE_PASS_LIMIT;
	}

	/**
	 * 작업 대기열로 전송
	 * @param gameId : 대기열로 이동할 게임 ID
	 * @param token : 대기열로 이동할 토큰
	 */
	public void moveToWorkingQueue(UUID gameId, String token) {
		String workingKey = WORKING_QUEUE_KEY_PREFIX + gameId;
		redisTemplate.opsForSet().add(workingKey, token);
		
		// TODO : 다음 요청으로 전송하는 로직 추가
		try {

		} finally {
			// 처리 후 working queue에서 제거
			redisTemplate.opsForSet().remove(workingKey, token);
		}
	}

	/**
	 * 해당 사용자가 작업 대기열에 존재하는지 확인
	 * @param gameId : 작업 대기열에 있는지 확인할 경기 ID
	 * @param token : 확인할 토큰
	 * @return : 토큰 존재 여부 반환
	 */
	public boolean isInWorkingQueue(UUID gameId, String token) {
		String workingKey = WORKING_QUEUE_KEY_PREFIX + gameId;
		Boolean result = redisTemplate.opsForSet().isMember(workingKey, token);
		return Boolean.TRUE.equals(result);
	}

	/**
	 * 전체 대기열 Queue의 사이즈 (MASTER 전용)
	 * @param gameId : 경기 ID
	 * @return : 경기 ID에 해당하는 전체 대기열 크기 반환
	 */
	public Long getQueueSize(UUID gameId) {
		String queueKey = QUEUE_KEY_PREFIX + gameId;
		return redisTemplate.opsForZSet().size(queueKey);
	}

	/**
	 * 해당 인원 수 만큼 Queue에 대기가 존재하는지 데이터 확인
	 * @param gameId : 경기 ID
	 * @param batchSize : 확인할 인원 수
	 * @return : 확인 결과 반환
	 */
	public Set<String> getRemainUsersInQueue(UUID gameId, int batchSize) {
		String queueKey = QUEUE_KEY_PREFIX + gameId;
		return redisTemplate.opsForZSet().range(queueKey, 0, batchSize - 1);
	}

	/**
	 * 경기 검색
	 * @param gameId : 경기 ID
	 * @return : 찾아낸 경기 반환
	 */
	private Game findGame(UUID gameId) {
		return gameRepository.findById(gameId)
			.orElseThrow(() -> new GameException(GameException.GameErrorType.GAME_NOT_FOUND));
	}

}
