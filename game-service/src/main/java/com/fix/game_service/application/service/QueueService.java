package com.fix.game_service.application.service;

import com.fix.game_service.application.exception.GameException;
import com.fix.game_service.domain.model.Game;
import com.fix.game_service.domain.repository.GameRepository;
import com.fix.game_service.infrastructure.kafka.QueueProducer;
import com.fix.game_service.presentation.scheduler.DeactivateGameScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.fix.game_service.application.exception.GameException.GameErrorType.GAME_CANNOT_RESERVATION;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueService {

    private final GameRepository gameRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final DeactivateGameScheduler deactivateGameScheduler;
    private final SimpMessagingTemplate messagingTemplate;
    private final String QUEUE_KEY_PREFIX = "queue:";
    private final String WORKING_QUEUE_KEY_PREFIX = "queue:working:";
    private final String ACTIVE_GAMES_KEY = "active-game";

    private final QueueProducer queueProducer;

    /**
     * 대기열 입장
     *
     * @param gameId : 입장할 경기 ID
     * @param userId : 입장 요청한 사용자 ID
     * @return : 토큰 반환
     */
    public Map<String, Object> enterQueue(UUID gameId, Long userId) {
        log.info("대기열 진입 시도: gameId={}, userId={}", gameId, userId);
        // 1. 해당 경기를 찾아서 예매 가능 시간인지 확인
        confirmActiveGame(gameId);

        // 2. 입장한 사용자에게 Token 발급
        String rawToken = UUID.randomUUID().toString();

        // 3. 발급한 토큰 Redis에 추가 (대기열)
        long timestamp = Instant.now().toEpochMilli();

        // 4. 해당 사용자 내용 Kakfa로 전송
        queueProducer.sendEnterQueue(gameId, rawToken, userId, timestamp);

        // 5. Map에 저장 (사용자에게 반환할 정보이므로 raw 자체로 반환)
        HttpHeaders headers = new HttpHeaders();
        headers.set("QueueToken", rawToken);

        // 6. 대기열에서의 순위 반환
        Map<String, Object> response = new HashMap<>();
        response.put("token", rawToken);

        return response;
    }

    /**
     * 활성화된 경기 확인
     *
     * @param gameId : 경기 ID
     */
    private void confirmActiveGame(UUID gameId) {
        // 1. Redis에 데이터가 있는지 확인
        Boolean isActive = redisTemplate.opsForSet().isMember(ACTIVE_GAMES_KEY, gameId.toString());
        LocalDateTime openDate = null;
        LocalDateTime closeDate = null;

        // 2. Redis에 데이터가 없다면
        if (!Boolean.TRUE.equals(isActive)) {
            Game game = findGame(gameId);
            openDate = game.getOpenDate();
            closeDate = game.getCloseDate();
            registerActiveGame(gameId, game.getStadiumId(), game.getOpenDate(), game.getCloseDate());
            // 3. Redis에 데이터가 있다면
        } else {
            Map<Object, Object> gameInfo = redisTemplate.opsForHash().entries(ACTIVE_GAMES_KEY + gameId);
            if (gameInfo != null && !gameInfo.isEmpty()) {
                openDate = LocalDateTime.parse((String) gameInfo.get("openDate"));
                closeDate = LocalDateTime.parse((String) gameInfo.get("closeDate"));
            }
        }

        if (openDate.isAfter(LocalDateTime.now()) ||
                closeDate.isBefore(LocalDateTime.now())) {
            throw new GameException(GAME_CANNOT_RESERVATION);
        }
    }

    /**
     * 현재 활성화된 경기 추가
     *
     * @param gameId    : 경기 ID
     * @param stadiumId : 경기장 ID
     * @param openDate  : 경기 예매 오픈 날짜
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
     * 해당 경기의 대기열에 남아있는 모든 토큰 가져오기
     *
     * @param gameId : 해당 경기 ID
     * @return : 토큰 Set 반환
     */
    public Set<String> getAllTokensInQueue(UUID gameId) {
        String queueKey = QUEUE_KEY_PREFIX + gameId;
        return redisTemplate.opsForZSet().range(queueKey, 0, -1);
    }

    /**
     * 현재 사용자의 대기 순서를 반환
     *
     * @param gameId : 경기 ID
     * @param token  : 대기열 token
     * @return : token에 해당하는 순서 반환
     */
    public Long getQueueNumber(UUID gameId, String token) {
        String queueKey = QUEUE_KEY_PREFIX + gameId;
        Long rank = redisTemplate.opsForZSet().rank(queueKey, token);
        return rank != null ? rank + 1 : null;
    }

    /**
     * 대기열을 떠나는 경우
     *
     * @param gameId : 경기 ID
     * @param token  : 떠나는 사용자의 토큰
     */
    public void leaveQueue(UUID gameId, String token) {
        String queueKey = QUEUE_KEY_PREFIX + gameId;
        redisTemplate.opsForZSet().remove(queueKey, token);
    }

    /**
     * 작업 대기열로 전송
     *
     * @param gameId : 대기열로 이동할 게임 ID
     * @param token  : 대기열로 이동할 토큰
     */
    @Async("queueExecutor")
    public void moveToWorkingQueue(UUID gameId, String token) {
        log.info("대기열에서 작업열로 이동 시작: gameId={}, token={}", gameId, token);
        long startTime = System.nanoTime();
        // 1. token 만료 시간 설정 (1시간 30분)
        long ttlInSeconds = 1 * 60 * 60 + 30 * 60;

        // 2. 해당 token 작업열에 저장 (token이 key가 됨)
        redisTemplate.opsForValue().set(WORKING_QUEUE_KEY_PREFIX + token, gameId.toString(), ttlInSeconds, TimeUnit.SECONDS);
        log.info("Redis 작업열에 저장 완료: key={}, value={}", WORKING_QUEUE_KEY_PREFIX + token, gameId);

        // 3. 대기 번호 전송 및 헤더에 token 삽입
        String userToken = token.split("\\|")[0];
        messagingTemplate.convertAndSend("/topic/queue/status/" + gameId + "/" + userToken, 1);

        long endTime = System.nanoTime();
        // 토큰(substring 된 상태)을 헤더에 담기
        HttpHeaders headers = new HttpHeaders();
        headers.set("QueueToken", userToken);
        log.info("대기열에서 작업열로 이동 완료: gameId={}, token={}, duration={}",
                gameId, token, TimeUnit.NANOSECONDS.toMillis(endTime - startTime));
    }

    /**
     * 작업열로 이동한 토큰을 대기열에서 제거
     *
     * @param gameId : 경기 ID
     * @param tokens : 제거할 토큰들
     */
    public void deleteTokensFromQueue(UUID gameId, Set<String> tokens) {
        String queueKey = QUEUE_KEY_PREFIX + gameId;
        redisTemplate.opsForZSet().remove(queueKey, tokens.toArray());
    }

    /**
     * 외부 요청에 의해 대기열을 떠나는 경우
     *
     * @param gameId : 경기 ID
     * @param userId : 떠나는 사용자 ID
     * @param token  : 떠나는 사용자의 Token
     */
    public void leaveQueueByRequest(UUID gameId, Long userId, String token) {
        String queueKey = QUEUE_KEY_PREFIX + gameId;
        String realToken = token + "|" + userId;
        redisTemplate.opsForZSet().remove(queueKey, realToken);
    }

    /**
     * 해당 사용자가 작업 대기열에 존재하는지 확인
     *
     * @param gameId : 작업 대기열에 있는지 확인할 경기 ID
     * @param userId : 사용자 ID
     * @param token  : 확인할 토큰
     * @return : 토큰 존재 여부 반환
     */
    public boolean isInWorkingQueue(UUID gameId, Long userId, String token) {
        String workingKey = WORKING_QUEUE_KEY_PREFIX + gameId;
        String realToken = token + "|" + userId;
        Boolean result = redisTemplate.opsForSet().isMember(workingKey, realToken);
        return Boolean.TRUE.equals(result);
    }

    /**
     * 전체 대기열 Queue의 사이즈 (MASTER 전용)
     *
     * @param gameId : 경기 ID
     * @return : 경기 ID에 해당하는 전체 대기열 크기 반환
     */
    public Long getQueueSize(UUID gameId) {
        String queueKey = QUEUE_KEY_PREFIX + gameId;
        return redisTemplate.opsForZSet().size(queueKey);
    }

    /**
     * 해당 인원 수 만큼 Queue에 대기가 존재하는지 데이터 확인
     *
     * @param gameId    : 경기 ID
     * @param batchSize : 확인할 인원 수
     * @return : 확인 결과 반환
     */
    public Set<String> getRemainUsersInQueue(UUID gameId, int batchSize) {
        String queueKey = QUEUE_KEY_PREFIX + gameId;
        return redisTemplate.opsForZSet().range(queueKey, 0, batchSize - 1);
    }

    /**
     * 경기 검색
     *
     * @param gameId : 경기 ID
     * @return : 찾아낸 경기 반환
     */
    private Game findGame(UUID gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new GameException(GameException.GameErrorType.GAME_NOT_FOUND));
    }
}
