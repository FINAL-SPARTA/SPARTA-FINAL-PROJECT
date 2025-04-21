package com.fix.user_service.application.service;

import com.fix.common_service.entity.UserRole;
import com.fix.user_service.application.dtos.request.UserCreateRequestDto;
import com.fix.user_service.application.dtos.request.UserSearchCondition;
import com.fix.user_service.application.dtos.request.UserUpdateRequestDto;
import com.fix.user_service.application.dtos.request.UserSearchRequestDto; // 🔧 추가
import com.fix.user_service.application.dtos.response.UserDetailResponseDto;
import com.fix.user_service.application.dtos.response.UserListResponseDto;
import com.fix.user_service.application.exception.UserException;
import com.fix.user_service.application.exception.UserException.UserErrorType;
import com.fix.user_service.domain.User;
import com.fix.user_service.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ✅ CREATE
    @Transactional
    public UserDetailResponseDto createUser(UserCreateRequestDto requestDto) {
        validateDuplicateUser(requestDto.getUsername(), requestDto.getEmail(),requestDto.getPhoneNumber());

        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

        User user = User.create(
            requestDto.getUsername(),
            requestDto.getEmail(),
            encodedPassword,
            requestDto.getNickname(),
            requestDto.getRoleName(),
            requestDto.getPhoneNumber()
        );

        return UserDetailResponseDto.from(userRepository.save(user));
    }

    // ✅ READ (단건)
    @Transactional(readOnly = true)
    public UserDetailResponseDto getUser(Long userId) {
        User user = findUserById(userId);
        return UserDetailResponseDto.from(user);
    }

    // ✅ READ (전체, 페이징)
    @Transactional(readOnly = true)
    public UserListResponseDto getAllUsers(Pageable pageable) {
        Page<User> page = userRepository.findAll(pageable);
        return new UserListResponseDto(page);
    }

    // ✅ 🔍 검색 기능 - keyword + role
    @Transactional(readOnly = true)
    public UserListResponseDto searchUsers(UserSearchRequestDto requestDto) {
        Page<User> result = userRepository.searchByKeyword(requestDto);
        return new UserListResponseDto(result);
    }
    // ✅ 관리자 검색 (조건 기반)
    @Transactional(readOnly = true)
    public UserListResponseDto searchUsersByCondition(UserSearchCondition condition, Pageable pageable) {
        Page<User> result = userRepository.searchByCondition(condition, pageable);
        return new UserListResponseDto(result);
    }
    // ✅ UPDATE
    @Transactional
    public UserDetailResponseDto updateUser(Long userId, UserUpdateRequestDto requestDto) {
        User user = findUserById(userId);
        UserRole role = parseRole(requestDto.getRoleName(), user.getRoleName());

        user.update(requestDto.getNickname(), requestDto.getEmail(), role,requestDto.getPhoneNumber());

        return UserDetailResponseDto.from(user);
    }

    // ✅ DELETE (soft delete)
    @Transactional
    public void deleteUser(Long userId) {
        User user = findUserById(userId);
        user.softDelete(userId);
        user.softDelete(0L); // TODO: 인증 적용 후 실제 로그인 유저 ID로 교체
    }

    // ✅ POST (포인트 차감)
    @Transactional
    public void deductPoints(Long userId, Integer requiredPoints) {
        User user = findUserById(userId);

        user.deductPoints(requiredPoints);
    }

    // 🔧 중복 검사
    private void validateDuplicateUser(String username, String email,String phoneNumber) {
        if (userRepository.existsByUsername(username)) {
            throw new UserException(UserErrorType.DUPLICATE_USERNAME);
        }
        if (userRepository.existsByEmail(email)) {
            throw new UserException(UserErrorType.DUPLICATE_EMAIL);
        }

        if(userRepository.existsByPhoneNumber(phoneNumber)){
            throw new UserException(UserErrorType.DUPLICATE_PHONE_NUMBER);
        }


    }
    // 역할 파싱
    private UserRole parseRole(String roleName, UserRole defaultRole) {
        if (roleName == null) return defaultRole;
        try {
            return UserRole.valueOf(roleName);
        } catch (IllegalArgumentException e) {
            throw new UserException(UserErrorType.INVALID_ROLE);
        }
    }

    // 닉네임 반환
    public String getNickname(Long userId) {
        System.out.println("사용자 ID : " + userId);
        User user = findUserById(userId);
        System.out.println("사용자 닉네임 : " + user.getNickname());
        return user.getNickname();
    }
    
    // 사용자 조회 공통 메서드
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new UserException(UserErrorType.USER_NOT_FOUND));
    }
}
