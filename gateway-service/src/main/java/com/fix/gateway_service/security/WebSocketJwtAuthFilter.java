package com.fix.gateway_service.security;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketJwtAuthFilter implements GatewayFilter {

	private final JwtUtil jwtUtil;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		ServerHttpRequest request = exchange.getRequest();
		ServerHttpResponse response = exchange.getResponse();

		String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			return onError(exchange, "토큰이 없습니다. ");
		}

		String token = authHeader.substring(7);

		try {
			Claims claims = jwtUtil.extractClaims(token);
			Integer userId = claims.get("userId", Integer.class);
			String userRole = claims.get("role", String.class);
			log.info("userId : {}, userRole : {}", userId, userRole);

			if (Objects.isNull(userId) || Objects.isNull(userRole)) {
				return onError(exchange, "토큰이 없습니다");
			}

			log.info("인증된 사용자 - ID: {}, Role: {}", userId, userRole);

			// 사용자 정보를 헤더에 추가
			ServerHttpRequest modifiedRequest = request.mutate()
				.headers(headers -> {
					headers.set("X-User-Id", userId.toString());
					headers.set("X-User-Role", userRole);
				})
				.build();

			return chain.filter(exchange.mutate().request(modifiedRequest).build())
				.contextWrite(context -> context.put("userId", userId).put("userRole", userRole));

		} catch (ExpiredJwtException e) {
			log.warn("JWT 만료: {}", e.getMessage());
			return onError(exchange, "토큰이 만료되었습니다.");
		} catch (JwtException | IllegalArgumentException e) {
			log.warn("JWT 검증 실패");
			return onError(exchange, "유효하지 않은 토큰입니다.");
		}

	}

	private Mono<Void> onError(ServerWebExchange exchange, String message) {
		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(HttpStatus.UNAUTHORIZED);
		response.getHeaders().add("Content-Type", "application/json");

		byte[] bytes = ("{\"error\": \"" + message + "\"}").getBytes(StandardCharsets.UTF_8);
		DataBuffer buffer = response.bufferFactory().wrap(bytes);

		return response.writeWith(Mono.just(buffer));
	}

}
