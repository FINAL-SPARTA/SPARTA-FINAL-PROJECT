package com.fix.user_service.domain.repository;

import com.fix.user_service.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {
    Optional<User> findByEmail(String email); // 로그인이나 중복체크용
    Optional<User> findByUsername(String username);
    Optional<User> findByNickname(String nickname);
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
