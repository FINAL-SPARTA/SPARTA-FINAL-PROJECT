package com.fix.user_service.presentation.controller;

import com.fix.common_service.dto.CommonResponse;
import com.fix.user_service.application.dtos.request.SignInRequestDTO;
import com.fix.user_service.application.dtos.response.SignInResponseDTO;
import com.fix.user_service.application.service.AuthApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthApplicationService authApplicationService;

    // ✅ 토큰 검증
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String token,
                                           @RequestHeader("requestUri") String requestUri) {
        try {
            if (requestUri.equals("/api/v1/auth/sign-up") || requestUri.equals("/api/v1/auth/sign-in")) {
                return ResponseEntity.ok().build();  // 인증 없이 진행
            }

            HttpHeaders headers = authApplicationService.validateToken(token);
            return ResponseEntity.ok().headers(headers).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token");
        }
    }

    // ✅ 로그인
    @PostMapping("/sign-in")
    public ResponseEntity<CommonResponse<SignInResponseDTO>> signIn(@RequestBody @Valid SignInRequestDTO signInRequest) {
        try {
            SignInResponseDTO response = authApplicationService.signIn(signInRequest);
            return ResponseEntity.ok(CommonResponse.success(response, "로그인 성공"));
        } catch (Exception e) {
            log.error("❌ 로그인 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(CommonResponse.fail(
                            "로그인 실패: " + e.getMessage(),
                            "AUTH_ERROR", // 또는 다른 에러 코드 네이밍
                            HttpStatus.UNAUTHORIZED.value()
                    ));
        }
    }

    // ✅ 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<CommonResponse<Void>> logout(@RequestParam String token) {
        authApplicationService.logout(token);
        return ResponseEntity.ok(CommonResponse.success(null, "로그아웃 성공"));
    }

    private Map<String, Object> ValidationErrorResponse(BindingResult bindingResult) {
        List<Map<String, String>> errors = bindingResult.getFieldErrors().stream()
                .map(fieldError -> Map.of(
                        "field", fieldError.getField(),
                        "message", fieldError.getDefaultMessage(),
                        "rejectedValue", String.valueOf(fieldError.getRejectedValue())
                ))
                .toList();

        return Map.of(
                "status", 400,
                "error", "Validation Field",
                "message", errors
        );
    }
}
