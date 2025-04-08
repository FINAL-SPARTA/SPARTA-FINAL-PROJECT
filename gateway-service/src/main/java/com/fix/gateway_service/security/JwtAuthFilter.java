package com.fix.gateway_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter implements GlobalFilter {

    private final JwtUtil jwtUtil;

    private final List<String> excludedRoutes = List.of(
            "/api/v1/auth/sign-in",
            "/api/v1/users/sign-up",
            "/actuator/health" // 헬스 체크도 인증 예외로 권장
    );

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        Predicate<ServerHttpRequest> isSecured = r ->
                excludedRoutes.stream().noneMatch(pattern -> pathMatcher.match(pattern, r.getURI().getPath()));

        if (isSecured.test(request)) {
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(response, "인증 토큰이 존재하지 않거나 형식이 잘못되었습니다.", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7); // "Bearer " 제거

            try {
                Claims claims = jwtUtil.extractClaims(token);
                Integer userId = claims.get("userId", Integer.class);
                String userRole = claims.get("role", String.class);
                log.info("userId : {}, userRole : {}", userId, userRole);

                if (Objects.isNull(userId) || Objects.isNull(userRole)) {
                    return onError(response, "JWT 클레임이 유효하지 않습니다.", HttpStatus.UNAUTHORIZED);
                }

                log.info("✅ 인증된 사용자 - ID: {}, Role: {}", userId, userRole);

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
                log.warn("🔒 JWT 만료: {}", e.getMessage());
                return onError(response, "토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED);
            } catch (JwtException | IllegalArgumentException e) {
                log.warn("🔒 JWT 검증 실패");
                return onError(response, "유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED);
            }
        }

        return chain.filter(exchange);
    }

    private Mono<Void> onError(ServerHttpResponse response, String message, HttpStatus status) {
        log.error("❌ 인증 오류: {}", message);
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String errorResponse = "{\"status\": \"" + status.value() + "\", \"message\": \"" + message + "\"}";
        log.error("status {}, message {}", status.value(), message);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(errorResponse.getBytes())));
    }
}
