package com.fix.user_service.presentation.controller;

import com.fix.common_service.dto.CommonResponse;
import com.fix.common_service.entity.UserRole;
import com.fix.user_service.application.dtos.request.UserCreateRequestDto;
import com.fix.user_service.application.dtos.request.UserSearchCondition;
import com.fix.user_service.application.dtos.request.UserSearchRequestDto;
import com.fix.user_service.application.dtos.request.UserUpdateRequestDto;
import com.fix.user_service.application.dtos.response.PhoneNumberResponseDto;
import com.fix.user_service.application.dtos.response.UserDetailResponseDto;
import com.fix.user_service.application.dtos.response.UserListResponseDto;
import com.fix.user_service.application.exception.UserException;
import com.fix.user_service.application.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 회원가입 (POST /users)
    @PostMapping("/sign-up")
    public ResponseEntity<UserDetailResponseDto> createUser(@Valid @RequestBody UserCreateRequestDto requestDto,
                                                            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body((UserDetailResponseDto) ValidationErrorResponse(bindingResult));
        }
        UserDetailResponseDto responseDto = userService.createUser(requestDto);
        return ResponseEntity.ok(responseDto);
    }

    // 특정 사용자 조회 - ID 기반 (GET /users/{userId})
    @GetMapping("/{userId}")
    public ResponseEntity<CommonResponse<UserDetailResponseDto>> getUserById(@PathVariable Long userId) {
        UserDetailResponseDto user = userService.getUser(userId);
        return ResponseEntity.ok(CommonResponse.success(user, "사용자 조회 성공"));
    }

    // 사용자 검색 (GET /users/search?keyword=xxx&role=MANAGER&page=0&size=10)
    @GetMapping("/search")
    public ResponseEntity<CommonResponse<UserListResponseDto>> searchUsers(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String role,
        Pageable pageable
    ) {
        UserRole userRole = role != null ? UserRole.valueOf(role.toUpperCase()) : null;

        UserSearchRequestDto searchRequest = UserSearchRequestDto.of(keyword, userRole, pageable);
        return ResponseEntity.ok(CommonResponse.success(
            userService.searchUsers(searchRequest),
            "사용자 검색 성공"
        ));
    }

    // 관리자용 고급 검색
    @GetMapping("/admin/search")
    public ResponseEntity<CommonResponse<UserListResponseDto>> searchUsersByCondition(
        @ModelAttribute UserSearchCondition condition,
        @RequestHeader("X-User-Role") String userRole,
        Pageable pageable
    ) {
        if (!UserRole.MASTER.name().equalsIgnoreCase(userRole)) {
            throw new UserException(UserException.UserErrorType.PERMISSION_DENIED);
        }

        return ResponseEntity.ok(CommonResponse.success(
            userService.searchUsersByCondition(condition, pageable),
            "관리자 사용자 검색 성공"
        ));
    }

    // 사용자 정보 업데이트 (PUT /users/{adminUserId}/{userId})
    @PutMapping("/{userId}")
    public ResponseEntity<CommonResponse<UserDetailResponseDto>> updateUser(
            @RequestHeader("X-User-Id") Long requesterId,
            @PathVariable Long userId,
            @RequestBody UserUpdateRequestDto requestDto
    ) {
        if (!requesterId.equals(userId)) {
            throw new UserException(UserException.UserErrorType.PERMISSION_DENIED);
        }

        return ResponseEntity.ok(CommonResponse.success(
                userService.updateUser(userId, requestDto),
                "사용자 정보 업데이트 성공"
        ));
    }

    // 사용자 논리 삭제 - Soft Delete (DELETE /users/{adminUserId}/{userId})
    @DeleteMapping("/{userId}")
    public ResponseEntity<CommonResponse<Void>> deleteUser(
            @RequestHeader("X-User-Id") Long requesterId,
            @PathVariable Long userId
    ) {
        if (!requesterId.equals(userId)) {
            throw new UserException(UserException.UserErrorType.PERMISSION_DENIED);
        }

        userService.deleteUser(userId);
        return ResponseEntity.ok(CommonResponse.success(null, "사용자가 삭제되었습니다."));
    }

    @PostMapping("/feign/deduct-points/{userId}")
    public ResponseEntity<CommonResponse<Void>> deductPoints(
            @PathVariable Long userId,
            @RequestParam Integer points
    ) {
        userService.deductPoints(userId, points);
        return ResponseEntity.ok(CommonResponse.success(null, "포인트 차감 성공"));
    }

    @GetMapping("/feign/{userId}/phone-number")
    public PhoneNumberResponseDto getPhoneNumber(@PathVariable("userId") Long userId){
        UserDetailResponseDto user = userService.getUser(userId);
        String phoneNumber = user.getPhoneNumber();
        return new PhoneNumberResponseDto(phoneNumber);

    }


    @GetMapping("/all")
    public ResponseEntity<CommonResponse<UserListResponseDto>> getAllUsers(
            @RequestHeader Map<String, String> headers,
            Pageable pageable
    ) {
        log.info("전달받은 전체 헤더: {}", headers); // ✅ 전체 헤더 찍기

        // x-user-id=1, x-user-role=MASTER
        String userRole = headers.getOrDefault("x-user-role", null); // 소문자 키로 접근
        log.info("추출된 userRole: {}", userRole);

        if (!UserRole.MASTER.name().equalsIgnoreCase(userRole)) {
            throw new UserException(UserException.UserErrorType.PERMISSION_DENIED);
        }

        return ResponseEntity.ok(CommonResponse.success(
                userService.getAllUsers(pageable),
                "전체 사용자 조회 성공"
        ));
    }

    @GetMapping("/{userId}/chat")
    public String getNickname(@PathVariable Long userId) {
        return userService.getNickname(userId);
    }

    private Map<String, Object> ValidationErrorResponse(BindingResult bindingResult) {
        List<Map<String, String>> errors = bindingResult.getFieldErrors().stream()
            .map(fieldError -> Map.of(
                "field", fieldError.getField(),
                "message", fieldError.getDefaultMessage(),
                "rejectedValue", String.valueOf(fieldError.getRejectedValue()) // 입력된 값도 포함
            ))
            .toList();

        return Map.of(
            "status", 400,
            "error", "Validation Field",
            "message", errors
        );
    }

}
