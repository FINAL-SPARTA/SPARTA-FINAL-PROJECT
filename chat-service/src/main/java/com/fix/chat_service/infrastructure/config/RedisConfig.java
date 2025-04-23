package com.fix.chat_service.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fix.chat_service.application.dtos.ChatMessageDto;
import com.fix.chat_service.presenatation.consumer.RedisConsumer;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {

	@Value("${spring.data.redis.host}")
	private String host;

	@Value("${spring.data.redis.port}")
	private int port;

	private final ObjectMapper objectMapper;

	/**
	 * Redis 연결 정보 설정
	 */
	@Bean
	public RedisConnectionFactory redisConnectionFactory() {
		RedisStandaloneConfiguration redisConfiguration = new RedisStandaloneConfiguration();
		redisConfiguration.setHostName(host);
		redisConfiguration.setPort(port);
		return new LettuceConnectionFactory(redisConfiguration);
	}

	/**
	 * Redis pub/sub 메세지 처리를 위한 Listener 설정
	 */
	@Bean
	public RedisMessageListenerContainer redisMessageListener(RedisConnectionFactory connectionFactory) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		return container;
	}

	/**
	 * redisTemplate 설정
	 */
	@Bean
	public RedisTemplate<String, ChatMessageDto> redisTemplate() {
		RedisTemplate<String, ChatMessageDto> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(redisConnectionFactory());
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		Jackson2JsonRedisSerializer<ChatMessageDto> serializer =
			new Jackson2JsonRedisSerializer<>(objectMapper, ChatMessageDto.class);
		redisTemplate.setValueSerializer(serializer);
		return redisTemplate;
	}

	@Bean
	public ChannelTopic topic() {
		return new ChannelTopic("chat-message");
	}

	@Bean
	public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory,
		RedisConsumer subscriber, ChannelTopic topic) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.addMessageListener(subscriber, topic);
		return container;
	}

}
