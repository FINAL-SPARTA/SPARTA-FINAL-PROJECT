package com.fix.user_service.application.service;

import com.fix.common_service.entity.UserRole;
import com.fix.user_service.application.dtos.request.CreateTokenDTO;
import com.fix.user_service.application.dtos.request.SignInRequestDTO;
import com.fix.user_service.application.dtos.response.SignInResponseDTO;
import com.fix.user_service.domain.User;
import com.fix.user_service.domain.repository.UserRepository;
import com.fix.user_service.infrastructure.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthApplicationService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenBlackListService tokenBlackListService;

    private static final String TRACE_ID = "traceId"; // MDC 키

    /**
     * 엑세스 토큰 생성
     * @param request : 토큰을 생성할 정보
     * @return : 생성한 토큰 반환
     */
    public String createAccessToken(CreateTokenDTO request) {
        return jwtTokenProvider.createAccessToken(request.getUserId(), request.getUsername(), request.getRole());
    }

    /**
     * 토큰 검증
     * @param token : 검증할 토큰
     * @return : 검증 결과
     */
    public HttpHeaders validateToken(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid token format");
        }

        token = token.substring(7);

        // 블랙리스트에 있으면 차단
        if (tokenBlackListService.isTokenBlackListed(token)) {
            throw new RuntimeException("Token is blacklisted");
        }

        try {
            Claims claims = jwtTokenProvider.validateTokenAndGetClaims(token);
            String userId = claims.get("userId", String.class);
            String username = claims.get("username", String.class);
            String roleString = claims.get("role", String.class);

            UserRole role = UserRole.valueOf(roleString);

            HttpHeaders headers = new HttpHeaders();
            headers.add("userId", userId);
            headers.add("username", username);
            headers.add("role", role.name());

            return headers;

        } catch (Exception e) {
            throw new RuntimeException("JWT 검증 실패: " + e.getMessage());
        }
    }


    /**
     * 로그인
     * @param signInRequest : 로그인 정보
     * @return : 로그인 결과
     */
    public SignInResponseDTO signIn(SignInRequestDTO signInRequest) {
        String traceId = MDC.get(TRACE_ID);
        log.info("[{}] 로그인 요청 시작 : username={}", traceId, signInRequest.getUsername());

        User user = userRepository.findByUsername(signInRequest.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다. Username: " + signInRequest.getUsername()));

        if (!passwordEncoder.matches(signInRequest.getPassword(), user.getPassword())) {
            log.warn("[{}] 로그인 실패 (비밀번호 불일치) : username={}", traceId, signInRequest.getUsername());
            throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");
        }

        String token = createAccessToken(new CreateTokenDTO(
                user.getUserId(),
                user.getUsername(),
                user.getRoleName()
        ));

        log.info("[{}] 로그인 성공 : userId={}, username={}", traceId, user.getUserId(), user.getUsername());

        return SignInResponseDTO.builder()
                .token(token)
                .userId(user.getUserId())
                .username(user.getUsername())
                .role(user.getRoleName().name())
                .build();

    }


    /**
     * 로그아웃
     * @param token : 로그아웃할 토큰
     */
    public void logout(String token) {
        token = token.substring(7);

        Claims claims = jwtTokenProvider.validateTokenAndGetClaims(token);
        Date expiration = claims.getExpiration();
        long now = System.currentTimeMillis();
        long remain = expiration.getTime() - now;

        tokenBlackListService.addTokenToBlackList(token, remain);
    }
}