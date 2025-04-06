package com.fix.user_service.infrastructure.security;

import com.fix.user_service.application.exception.UserException;
import com.fix.user_service.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.fix.user_service.domain.User;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    // 사용자명으로 사용자를 검색하는 메서드
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UserException(UserException.UserErrorType.USER_NOT_FOUND));

        return new UserDetailsImpl(user);
    }

    // 사용자ID로 사용자를 검색하는 메서드
    public UserDetails loadUserById(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserException(UserException.UserErrorType.USER_NOT_FOUND));

        return new UserDetailsImpl(user);
    }
}
