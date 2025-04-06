package com.fix.user_service.domain;

import com.fix.common_service.entity.UserRole;
import com.fix.common_service.entity.Basic;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_user")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class User extends Basic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nickname; // ✅ 추가된 필드

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole roleName;

//    빌더 패턴
    @Builder
    private User(String username, String email, String password, String nickname, UserRole roleName) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.roleName = roleName;
    }


//    정적팩토리 메서드
    public static User create(String username, String email, String encodedPassword, String nickname, UserRole roleName) {
        return User.builder()
            .username(username)
            .email(email)
            .password(encodedPassword)
            .nickname(nickname)
            .roleName(roleName)
            .build();
    }

//    수정 메서드
    public void update(String nickname, String email, UserRole roleName) {
        this.nickname = nickname;
        this.email = email;
        this.roleName = roleName;
    }
}
