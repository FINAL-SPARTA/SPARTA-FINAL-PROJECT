package com.fix.user.application.service;

import com.fix.common_service.entity.UserRole;
import com.fix.user.application.dtos.request.CreateTokenDTO;
import com.fix.user.application.dtos.request.SignInRequestDTO;
import com.fix.user.domain.User;
import com.fix.user.domain.repository.UserRepository;
import com.fix.user.infrastructure.security.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthApplicationService {

    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenBlackListService tokenBlackListService;

    /**
     * 엑세스 토큰 생성
     * @param request : 토큰을 생성할 정보
     * @return : 생성한 토큰 반환
     */
    public String createAccessToken(CreateTokenDTO request) {
        return jwtUtils.createAccessToken(request.getUserId(), request.getUsername(), request.getRole());
    }

    /**
     * 토큰 검증
     * @param token : 검증할 토큰
     * @return : 검증 결과
     */
    public HttpHeaders validateToken(String token) {
        if (token == null || !token.startsWith("Bearer ") || !tokenBlackListService.isTokenBlackListed(token)) {
            throw new RuntimeException("Invalid token");
        }
        token = token.substring(7);
        try {
            Claims claims = jwtUtils.getClaims(token);
            String userId = claims.get("userId", String.class);
            String username = claims.get("username", String.class);
            String roleString = claims.get("role", String.class);

            UserRole role = UserRole.valueOf(roleString);

            HttpHeaders headers = new HttpHeaders();
            // 검증된 정보를 응답 헤더에 추가
            headers.add("userId", userId);
            headers.add("username", username);
            headers.add("role", role.toString());

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
    public String signIn(SignInRequestDTO signInRequest) {
        try {
            User user = userRepository.findByUsername(signInRequest.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

            if (!passwordEncoder.matches(signInRequest.getPassword(), user.getPassword())){
                throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");
            }
            return createAccessToken(new CreateTokenDTO(user.getUserId(), user.getUsername(), user.getRoleName()));
        }
        catch (Exception e) {
            throw e;
        }
    }

    /**
     * 로그아웃
     * @param token : 로그아웃할 토큰
     */
    public void logout(String token) {
        token = token.substring(7);

        // 남은 만료 시간 계산
        Claims claims = jwtUtils.getClaims(token);
        Date expiration = claims.getExpiration();
        long now = System.currentTimeMillis();
        long exp = expiration.getTime();
        long remain = exp - now;

        tokenBlackListService.addTokenToBlackList(token, remain);
    }
}