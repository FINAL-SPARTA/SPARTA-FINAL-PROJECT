package com.fix.chat_service.presenatation.consumer;

import java.util.UUID;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fix.chat_service.application.dtos.ChatMessage;
import com.fix.chat_service.infrastructure.handler.CustomWebSocketHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisConsumer implements MessageListener {

	private final RedisTemplate redisTemplate;
	private final CustomWebSocketHandler webSocketHandler;

	@Override
	public void onMessage(Message message, byte[] pattern) {
		try {
			ChatMessage chatMessage = (ChatMessage)redisTemplate.getValueSerializer().deserialize(message.getBody());
			log.info("Redis Consumer로 메시지 넘어옴 : {}", chatMessage.getMessage());
			webSocketHandler.broadcastMessage(UUID.fromString(chatMessage.getChatId()), chatMessage);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}
}
