package com.fix.chat_service.infrastructure.handler;

import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.fix.chat_service.infrastructure.client.UserClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomHandshakeInterceptor implements HandshakeInterceptor {

	private final UserClient userClient;

	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
		WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

		if (request instanceof ServletServerHttpRequest servletServerHttpRequest) {
			String userId = servletServerHttpRequest.getServletRequest().getHeader("x-user-id");
			log.info("userID: {}" ,userId);

			if (userId != null) {
				Long rawUserId = Long.valueOf(userId);
				try {
					String nickname = userClient.getNickname(rawUserId);
					attributes.put("nickname", nickname);
				} catch (Exception e) {
					attributes.put("nickname", "Anonymous");
				} finally {
					attributes.put("userId", rawUserId);
				}
			}
		}
		return true;
	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
		WebSocketHandler wsHandler, Exception exception) {}
}
