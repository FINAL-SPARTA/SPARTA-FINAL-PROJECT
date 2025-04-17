package com.fix.common_service.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisIdempotencyChecker implements IdempotencyChecker {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String IDEMPOTENCY_KEY_PREFIX = "kafka:idempotency:";
    private static final long ttlDays = 1L; // 1일


    @Override
    public boolean isNew(String messageKey) {
        String redisKey = IDEMPOTENCY_KEY_PREFIX + messageKey;
        Duration timeout = Duration.ofDays(ttlDays);

        // setIfAbsent 메서드를 사용하여 Redis에 키를 설정하고, 이미 존재하는 경우 false를 반환
        Boolean wasSet = stringRedisTemplate.opsForValue().setIfAbsent(redisKey, "1", timeout);

        if (Boolean.TRUE.equals(wasSet)) {
            // 새로운 키 설정 (새 메시지인 경우)
            log.debug("새로운 메시지 키 등록 성공 (멱등성 체크 통과). Key: '{}', TTL: {} days", redisKey, ttlDays);
            return true;
        } else {
            // 키가 이미 존재함 (중복 메시지)
            log.warn("이미 처리된 메시지입니다 (Redis 멱등성 체크). Key: '{}'. Skipping.", redisKey);
            return false;
        }
    }

    @Override
    public void markProcessed(String messageKey) {
        String redisKey = IDEMPOTENCY_KEY_PREFIX + messageKey;
        Duration timeout = Duration.ofDays(ttlDays);
        try {
            // 키가 존재하든 안하든 값을 "1"로 설정하고 TTL 적용 (갱신 효과)
            stringRedisTemplate.opsForValue().set(redisKey, "1", timeout);
            log.debug("메시지 처리 완료 마킹 (Redis TTL 갱신). Key: '{}'", redisKey);
        } catch (Exception e) {
            // Redis 작업 실패 시 로깅 (필요시 추가 에러 처리)
            log.error("Redis에 처리 완료 마킹 중 오류 발생. Key: '{}'. Error: {}", redisKey, e.getMessage(), e);
        }
    }
}
