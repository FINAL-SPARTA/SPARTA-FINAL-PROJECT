package com.fix.user_service.presentation.controller;

import com.fix.common_service.dto.CommonResponse;
import com.fix.common_service.entity.UserRole;
import com.fix.user_service.application.dtos.request.UserCreateRequestDto;
import com.fix.user_service.application.dtos.request.UserSearchCondition;
import com.fix.user_service.application.dtos.request.UserSearchRequestDto;
import com.fix.user_service.application.dtos.request.UserUpdateRequestDto;
import com.fix.user_service.application.dtos.response.UserDetailResponseDto;
import com.fix.user_service.application.dtos.response.UserListResponseDto;
import com.fix.user_service.application.exception.UserException;
import com.fix.user_service.application.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
        @PathVariable Long userId,
        @RequestBody UserUpdateRequestDto requestDto) {
        return ResponseEntity.ok(CommonResponse.success(
            userService.updateUser(userId, requestDto),
            "사용자 정보 업데이트 성공")
        );
    }

    // 사용자 논리 삭제 - Soft Delete (DELETE /users/{adminUserId}/{userId})
    @DeleteMapping("/{userId}")
    public ResponseEntity<CommonResponse<Void>> deleteUser(
        @PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(CommonResponse.success(
            null, // Data Type ?
            "사용자가 삭제되었습니다.")
        );
    }

    @GetMapping("/all")
    public ResponseEntity<CommonResponse<UserListResponseDto>> getAllUsers(
        @RequestHeader("X-User-Role") String userRole,
        Pageable pageable) {

        if (!UserRole.MASTER.name().equalsIgnoreCase(userRole)) {
            throw new UserException(UserException.UserErrorType.PERMISSION_DENIED);
        }

        return ResponseEntity.ok(CommonResponse.success(
            userService.getAllUsers(pageable),
            "전체 사용자 조회 성공"
        ));
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
