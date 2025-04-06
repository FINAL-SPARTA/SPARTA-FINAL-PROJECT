package com.fix.user_service.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class TokenBlackListService {

    private static final String BLACKLIST_PREFIX = "logout:";

    private final RedisTemplate<String, Object> redisTemplate;

    // ✅ Redis 블랙리스트에 토큰 추가
    public void addTokenToBlackList(String token, long remain) {
        String key = BLACKLIST_PREFIX + token;
        redisTemplate.opsForValue().set(key, "LOGOUT", remain, TimeUnit.MILLISECONDS);
    }

    // ✅ Redis 블랙리스트에 토큰이 존재하는지 체크
    public boolean isTokenBlackListed(String token) {
        String key = BLACKLIST_PREFIX + token;
        return redisTemplate.hasKey(key) == Boolean.TRUE;
    }
}
