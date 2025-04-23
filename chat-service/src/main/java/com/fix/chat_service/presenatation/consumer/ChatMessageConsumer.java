package com.fix.chat_service.presenatation.consumer;

import java.io.IOException;
import java.util.UUID;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fix.chat_service.application.dtos.ChatMessageDto;
import com.fix.chat_service.infrastructure.handler.CustomWebSocketHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatMessageConsumer {

	private final CustomWebSocketHandler webSocketHandler;
	private final ObjectMapper objectMapper;

	/**
	 * 토픽으로 메시지 전송
	 * @param message : 받은 메시지 해당 채팅방으로 전달
	 */
	@KafkaListener(topics = "${kafka-topics.chat.message}", groupId = "${spring.kafka.consumer.group-id}")
	public void consumeMessage(ChatMessageDto message) throws IOException {
		// 로그 저장 등의 로직이 필요하다면 ChatMessage로 캐스팅 과정 필요
		// ChatMessage chatMessage = objectMapper.readValue(message, ChatMessage.class);

		// 메시지 브로드캐스트
		webSocketHandler.broadcastMessage(UUID.fromString(message.getChatId()), message);
	}
}