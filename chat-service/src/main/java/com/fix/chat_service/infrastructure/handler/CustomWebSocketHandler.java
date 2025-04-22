package com.fix.chat_service.infrastructure.handler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fix.chat_service.application.dtos.ChatMessage;
import com.fix.chat_service.presenatation.producer.ChatMessageProducer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomWebSocketHandler extends TextWebSocketHandler {

	// 채팅방 별 세션 저장
	private final Map<UUID, List<WebSocketSession>> chatRooms = new ConcurrentHashMap<>();
	// 채팅 생성
	private final ChatMessageProducer producer;
	private final ObjectMapper objectMapper;

	@Value("${kafka-topics.chat.message}")
	private String chatMessageTopic;

	/**
	 * 초반 WebSocket 세션 연결
	 * @param session : 연결할 세션
	 */
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		UUID chatId = getChatId(session);
		chatRooms.putIfAbsent(chatId, new CopyOnWriteArrayList<>());
		chatRooms.get(chatId).add(session);

		log.info("새 연결: {} (chatId={})", session.getId(), chatId);
	}

	/**
	 * 메시지를 관리
	 * @param session : 메시지를 관리할 세션
	 * @param message : 전달할 메시지
	 */
	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		UUID chatId = getChatId(session);
		ChatMessage chatMessage = objectMapper.readValue(message.getPayload(), ChatMessage.class);
		String nickname = (String) session.getAttributes().get("nickname");
		chatMessage.setMessageType("USER");
		chatMessage.setChatId(chatId.toString());
		chatMessage.setNickname(nickname);
		producer.sendMessage(chatMessageTopic, chatMessage);
	}

	/**
	 * 메시지 전송
	 * @param chatId : 채팅방 ID
	 * @param message : 전달할 메시지
	 */
	public void broadcastMessage(UUID chatId, ChatMessage message) throws IOException {
		List<WebSocketSession> sessions = chatRooms.get(chatId);
		String chatMessage = objectMapper.writeValueAsString(message);
		if (sessions != null) {
			for (WebSocketSession session : sessions) {
				if (session.isOpen()) {
					try {
						session.sendMessage(new TextMessage(chatMessage));
					} catch (Exception e) {
						log.warn("세션 오류로 메시지 전송 실패. 세션 강제 종료: {}, 오류: {}", session.getId(), e.getMessage());
						session.close();
					}
				}
			}
		}
	}

	/**
	 * 연결이 끊어지는 경우
	 * @param session : 연결이 끊어진 세션
	 * @param status : 끊어진 상태
	 */
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		super.afterConnectionClosed(session, status);
		UUID chatId = getChatId(session);
		chatRooms.get(chatId).remove(session);
		log.info("연결 종료: {} (chatId={})", session.getId(), chatId);
	}

	/**
	 * URI에서 채팅방 ID 추출
	 * @param session : 세션 정보
	 * @return : 세션에서 추출한 채팅방 ID
	 */
	private UUID getChatId(WebSocketSession session) {
		String path = session.getUri().getPath();
		String chatIdStr = path.substring(path.lastIndexOf("/") + 1);
		return UUID.fromString(chatIdStr);
	}

}
