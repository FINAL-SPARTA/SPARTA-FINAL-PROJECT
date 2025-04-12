package com.fix.game_service.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		// WebSocket 연결 endpoint 정의
		registry.addEndpoint("/ticketing")
			.setAllowedOriginPatterns("*") // CORS 허용 (배포때는 수정하기)
			.withSockJS(); // SockJS 지원
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		// 서버가 클라이언트로 메시지 보낼 때 prefix
		config.enableSimpleBroker("/topic");
		// 클라이언트에서 서버로 메시지 보낼 때 prefix
		config.setApplicationDestinationPrefixes("/app");
	}

}

