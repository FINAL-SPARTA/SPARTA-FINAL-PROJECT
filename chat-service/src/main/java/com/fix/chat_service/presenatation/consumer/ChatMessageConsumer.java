package com.fix.chat_service.presenatation.consumer;

import java.io.IOException;
import java.util.UUID;

import org.json.JSONObject;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fix.chat_service.infrastructure.handler.CustomWebSocketHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatMessageConsumer {

	private final CustomWebSocketHandler webSocketHandler;

	/**
	 * 토픽으로 메시지 전송
	 * @param message : 받은 메시지 해당 채팅방으로 전달
	 */
	@KafkaListener(topics = "chat-message", groupId = "chat-service")
	public void consume(String message) throws IOException {
		JSONObject json = new JSONObject(message);
		String chatIdStr = json.getString("chatId");

		// 메시지 브로드캐스트
		webSocketHandler.broadcastMessage(UUID.fromString(chatIdStr), message);
	}
}