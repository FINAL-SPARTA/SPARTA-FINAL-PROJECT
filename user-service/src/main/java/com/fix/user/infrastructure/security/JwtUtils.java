package com.fix.user.infrastructure.security;

import com.fix.common_service.entity.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtils {
    @Value("${spring.application.name}")
    private String issuer;

    @Value("${service.jwt.access-expiration}")
    private Long accessExpiration;

    private final SecretKey secretKey;

    public JwtUtils(@Value("${service.jwt.secret-key}") String secretKey) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(secretKey));
    }

    public Long getUserId(String token) {
        Claims claims = getClaims(token);
        Long userId = claims.get("userId", Long.class);

        return userId;
    }

    public UserRole getUserRole(String token) {
        Claims claims = getClaims(token);
        String role = claims.get("role", String.class);
        return UserRole.valueOf(role);
    }

    public String getUsername(String token) {
        Claims claims = getClaims(token);
        return claims.get("username", String.class);
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
            .setSigningKey(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public String createAccessToken(Long userId, String username, UserRole userRole) {
        return Jwts.builder()
            .claim("userId", userId)
            .claim("username", username)
            .claim("role", userRole)
            .issuer(issuer)
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis() + accessExpiration))
            .signWith(secretKey, SignatureAlgorithm.HS512)
            .compact();
    }
}
