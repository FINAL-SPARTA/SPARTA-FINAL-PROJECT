package com.fix.order_service.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fix.order_service.application.dtos.request.OrderSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderHistoryRedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private ObjectMapper objectMapper;

    public void saveRecentOrder(Long userId, OrderSummaryDto summary) {
        String key = "order:recent:" + userId;

        redisTemplate.opsForList().leftPush(key, summary);
        redisTemplate.opsForList().trim(key, 0, 9); // 최근 10개만 유지
        redisTemplate.expire(key, Duration.ofDays(7)); // TTL 7일 (선택)
    }

//    Redis에서 유저의 최근 주문 내역 조회
    public List<OrderSummaryDto> getRecentOrders(Long userId) {
        String key = "order:recent:" + userId;
        List<Object> result = redisTemplate.opsForList().range(key, 0, 9);

        return result.stream()
                .map(obj -> objectMapper.convertValue(obj, OrderSummaryDto.class))
                .toList();
    }
}
