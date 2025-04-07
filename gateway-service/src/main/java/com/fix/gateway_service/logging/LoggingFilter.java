package com.fix.gateway_service.logging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        log.info("➡️ [Gateway 요청] {} {}", request.getMethod(), request.getURI());
        log.debug("📝 [요청 헤더] {}", request.getHeaders());

        // 응답 이후 상태코드 로깅 (doAfterTerminate → doFinally 대체로 더 안정적)
        return chain.filter(exchange)
                .doFinally(signalType -> {
                    ServerHttpResponse response = exchange.getResponse();
                    if (response.getStatusCode() != null) {
                        log.info("⬅️ [Gateway 응답] 상태 코드: {}", response.getStatusCode().value());
                    } else {
                        log.warn("⬅️ [Gateway 응답] 상태 코드 없음");
                    }
                });
    }

    @Override
    public int getOrder() {
        return -1; // 가장 먼저 실행
    }
}
