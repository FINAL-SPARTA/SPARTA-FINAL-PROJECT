package com.fix.gateway_service.config;

import com.fix.gateway_service.security.JwtAuthFilter;
import com.fix.gateway_service.security.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;

import java.util.function.Predicate;

@Slf4j
@Configuration
public class GatewayConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public GatewayConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    private static final String[] USER_PATHS = {
            "/api/v1/users/**",
            "/api/v1/auth/**"
    };

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()

                .route("user-service", r -> r.path(USER_PATHS)
                        .filters(this::applyFilters)
                        .uri("lb://user-service"))

                .route("alarm-service", r -> r.path("/api/v1/alarms/**")
                        .filters(this::applyFilters)
                        .uri("lb://alarm-service"))

                .route("event-service", r -> r.path("/api/v1/events/**")
                        .filters(this::applyFilters)
                        .uri("lb://event-service"))

                .route("game-service", r -> r.path("/api/v1/games/**")
                        .filters(this::applyFilters)
                        .uri("lb://game-service"))

                .route("order-service", r -> r.path("/api/v1/orders/**")
                        .filters(this::applyFilters)
                        .uri("lb://order-service"))

                .route("stadium-service", r -> r.path("/api/v1/stadiums/**")
                        .filters(this::applyFilters)
                        .uri("lb://stadium-service"))

                .route("ticket-service", r -> r.path("/api/v1/tickets/**")
                    .filters(this::applyFilters)
                    .uri("lb://ticket-service"))

                .route("alarm-service", r -> r.path("/api/v1/alarms/**")
                        .filters(this::applyFilters)
                        .uri("lb://alarm-service"))

                .route("fallback-route", r -> r.path("/fallback")
                        .uri("no://op"))

                .build();
    }

    /**
     * 공통 필터(JWT 인증 필터 등) 적용
     */
    private GatewayFilterSpec applyFilters(GatewayFilterSpec f) {
        // GlobalFilter를 직접 GatewayFilter로 캐스팅하는 방식 대신 람다로 위임
        return f.filter((exchange, chain) -> jwtAuthFilter.filter(exchange, chain));
    }


}