package com.fix.chat_service.presenatation.producer;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import com.fix.chat_service.application.dtos.ChatMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisPublisher {

	private final RedisTemplate<String, ChatMessage> redisTemplate;

	public void publish(ChannelTopic topic, ChatMessage message) {
		log.info("Redis Publisher로 메시지 넘어옴 : {}", topic.getTopic());
		redisTemplate.convertAndSend(topic.getTopic(), message);
	}

}
