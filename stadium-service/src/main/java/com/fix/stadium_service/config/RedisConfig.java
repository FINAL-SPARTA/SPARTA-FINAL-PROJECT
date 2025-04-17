package com.fix.stadium_service.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;


    // Redis 서버와 연결하기 위한 ConnectionFactory 를 생성
    @Bean
    public RedisConnectionFactory redisConnectionFactory(){
        // Redis 단일 인스턴스 설정 (클러스터나 Sentinel 이 아닌 경우)
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(host,port);
        //Lettuce 클라이언트 사용 (비동기 처리에 적합, 실무에서 널리 사용)
        return new LettuceConnectionFactory(redisStandaloneConfiguration);
    }

    // RedisTemplate : Redis 와 직접 데이터를 주고받을 수 있는 도구
    @Bean
    public RedisTemplate<String,Object> redisTemplate(){
        RedisTemplate<String,Object> redisTemplate = new RedisTemplate<>();

        // 위에서 만든 커넥션 팩토리 주입
        redisTemplate.setConnectionFactory(redisConnectionFactory());

        //Redis 의 Key는 문자열로 저장
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        // Redis 의 value 는 JSON 형태로 직렬화(객체 - > 문자열)
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        return redisTemplate;
    }

    // Spring Cache 를 사용할때 동작할 캐시 매니저 정의
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory){

        //캐시에 저장된 값들을 직렬화하고 TTL 설정
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())
                )
                .entryTtl(Duration.ofMinutes(120)) // 캐시 수명 120분
                .disableCachingNullValues(); // null 값은 캐싱하지 않도록 설정

        Map<String,RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        //TTL 1일
        cacheConfigs.put("seatSectionsCache",redisCacheConfiguration.entryTtl(Duration.ofDays(1)));





        //설정된 cacheConfig를 기반으로 RedisCacheManager를 생성
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(redisCacheConfiguration)
                .build();


    }


}
