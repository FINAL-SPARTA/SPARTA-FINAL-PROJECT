package com.fix.game_service.infrastructure.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.CacheKeyPrefix;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
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

	@Bean
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(factory);
		template.setKeySerializer(new StringRedisSerializer());
		return template;
	}

	@Bean
	public CacheManager cacheManager(RedisConnectionFactory connectionFactory){
		// Cache에 저장된 값 직렬화 및 TTL 설정
		RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
				.serializeValuesWith(
						RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())
				)
				.entryTtl(Duration.ofMinutes(120))
				.disableCachingNullValues();

		// 설정한 CacheConfig 기반으로 RedisCacheManager 생성
		return RedisCacheManager.builder(connectionFactory)
				.cacheDefaults(redisCacheConfiguration)
				.build();

	}

}