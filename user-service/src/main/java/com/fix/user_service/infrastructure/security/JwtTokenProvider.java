package com.fix.user_service.infrastructure.security;

import com.fix.common_service.entity.UserRole;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;

    @Value("${spring.application.name}")
    private String issuer;

    @Value("${service.jwt.access-expiration}")
    private long accessTokenValidity;

    @Value("${service.jwt.refresh-expiration}")
    private long refreshTokenValidity;

    public JwtTokenProvider(@Value("${service.jwt.secret-key}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(secret));
    }

    // ✅ 액세스 토큰 생성
    public String createAccessToken(Long userId, String username, UserRole role) {
        return createToken(userId, username, role.name(), accessTokenValidity);
    }

    // ✅ 리프레시 토큰 생성
    public String createRefreshToken(Long userId, String username) {
        return createToken(userId, username, "REFRESH", refreshTokenValidity);
    }

    // ✅ 공통 토큰 생성
    private String createToken(Long userId, String username, String role, long validity) {
        return Jwts.builder()
                .claim("userId", userId)
                .claim("username", username)
                .claim("role", role)
                .issuer(issuer)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + validity))
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    // ✅ 토큰 유효성 검사 및 Claims 반환
    public Claims validateTokenAndGetClaims(String token) {
        try {
            return parseClaims(token);
        } catch (JwtException | IllegalArgumentException e) {
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    // ✅ 단순 유효성 검사 (true/false)
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // ✅ Claims 파싱 공통 처리
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // ✅ 개별 Claim 추출 메서드들
    public Long getUserIdFromToken(String token) {
        return parseClaims(token).get("userId", Long.class);
    }

    public String getUsernameFromToken(String token) {
        return parseClaims(token).get("username", String.class);
    }

    public UserRole getUserRoleFromToken(String token) {
        String role = parseClaims(token).get("role", String.class);
        return UserRole.valueOf(role);
    }
}
