package com.fix.gateway_service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.logging.Logger;

@Slf4j
@Component
public class CustomPreFilter implements GlobalFilter, Ordered {
    private static final Logger logger = Logger.getLogger(CustomPreFilter.class.getName());
    private final WebClient webClient;

    public CustomPreFilter(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:19092").build(); // 인증 서버 주소
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        logger.info("URI:  " + request.getURI());

        // 회원가입 요청은 인증 제외
        if (request.getURI().getPath().equals("/api/v1/users/sign-up") || request.getURI().getPath().equals("/api/v1/auth/sign-in")
            || request.getURI().getPath().equals("/swagger-ui/**")) {
            return chain.filter(exchange);
        }

        String token = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (token == null || !token.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // 인증 서버에 검증 요청
        return webClient.post()
            .uri("/api/v1/auth/validate")
            .header(HttpHeaders.AUTHORIZATION, token)
            .header("requestUri", request.getURI().getPath())
            .retrieve()
            .toBodilessEntity()
            .flatMap(response -> {
                // 응답 헤더에서 사용자 정보 추출
                HttpHeaders headers = response.getHeaders();

                logger.info("인증 서버 응답 헤더: " + headers);
                logger.info("userId: " + headers.getFirst("userId"));
                logger.info("username: " + headers.getFirst("username"));
                logger.info("role: " + headers.getFirst("role"));

                ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                    .header("userId", headers.getFirst("userId"))
                    .header("username", headers.getFirst("username"))
                    .header("role", headers.getFirst("role"))
                    .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            })
            .onErrorResume(e -> {
                logger.warning("인증 실패: " + e.getMessage());
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
