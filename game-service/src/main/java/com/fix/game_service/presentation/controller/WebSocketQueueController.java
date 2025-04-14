package com.fix.game_service.presentation.controller;

import java.util.Map;
import java.util.UUID;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.fix.game_service.application.service.QueueService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketQueueController {

	private final QueueService queueService;

	// TODO : 서버 스케일아웃 시 수정이 필요한 부분 (다른 방법 생각해보기)

	@EventListener
	public void handleSessionConnect(SessionConnectEvent event) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
		String token = accessor.getFirstNativeHeader("token");
		String gameId = accessor.getFirstNativeHeader("gameId");

		if (token != null && gameId != null) {
			accessor.getSessionAttributes().put("token", token);
			accessor.getSessionAttributes().put("gameId", gameId);
		}
	}

	@EventListener
	public void handleSessionDisconnect(SessionDisconnectEvent event) {
		log.info("세션 접근 종료 이벤트 수신");
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

		String token = (String) accessor.getSessionAttributes().get("token");
		String gameId = (String) accessor.getSessionAttributes().get("gameId");

		if (token != null && gameId != null) {
			queueService.leaveQueue(UUID.fromString(gameId), token);
			log.info("대기열에서 제거 완료 : token = " + token);
		}
	}

}
