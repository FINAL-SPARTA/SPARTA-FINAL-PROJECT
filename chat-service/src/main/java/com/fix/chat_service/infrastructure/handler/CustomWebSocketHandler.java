package com.fix.chat_service.infrastructure.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomWebSocketHandler extends TextWebSocketHandler {

	// 채팅방 별 세션 저장
	private final Map<UUID, List<WebSocketSession>> chatRooms = new ConcurrentHashMap<>();

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		UUID chatId = getChatId(session);
		chatRooms.putIfAbsent(chatId, new CopyOnWriteArrayList<>());
		chatRooms.get(chatId).add(session);

		log.info("새 연결: {} (chatId={})", session.getId(), chatId);
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		String payload = message.getPayload();
		log.info("수신 메시지: {}", payload);

		UUID chatId = getChatId(session);

		try {
			JSONObject json = new JSONObject(payload);
			String nickname = json.optString("nickname", "익명");
			String msg = json.optString("message");

			JSONObject result = new JSONObject();
			result.put("nickname", nickname);
			result.put("message", msg);

			// 같은 채팅방 세션에게만 브로드캐스트
			for (WebSocketSession s : chatRooms.get(chatId)) {
				if (s.isOpen()) {
					s.sendMessage(new TextMessage(result.toString()));
				}
			}

		} catch (JSONException e) {
			log.error("JSON 파싱 오류", e);
			JSONObject error = new JSONObject();
			error.put("nickname", "System");
			error.put("message", "메시지 처리 중 오류 발생");

			session.sendMessage(new TextMessage(error.toString()));
		}
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		UUID chatId = getChatId(session);
		chatRooms.get(chatId).remove(session);
		log.info("연결 종료: {} (chatId={})", session.getId(), chatId);
	}

	// URI에서 chatId 추출
	private UUID getChatId(WebSocketSession session) {
		String path = session.getUri().getPath();
		String chatIdStr = path.substring(path.lastIndexOf("/") + 1);
		return UUID.fromString(chatIdStr);
	}
}
