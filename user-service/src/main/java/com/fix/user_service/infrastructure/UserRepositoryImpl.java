package com.fix.user_service.infrastructure;

import com.fix.user_service.domain.QUser;
import com.fix.user_service.domain.User;
import com.fix.user_service.application.dtos.request.UserSearchRequestDto;
import com.fix.user_service.application.dtos.request.UserSearchCondition;
import com.fix.user_service.domain.repository.UserRepositoryCustom;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    // ✅ 사용자 키워드 검색
    @Override
    public Page<User> searchByKeyword(UserSearchRequestDto requestDto) {
        QUser user = QUser.user;
        BooleanBuilder builder = new BooleanBuilder();

        String keyword = requestDto.getKeyword();
        if (keyword != null && !keyword.isBlank()) {
            builder.and(
                user.username.containsIgnoreCase(keyword)
                    .or(user.email.containsIgnoreCase(keyword))
                    .or(user.nickname.containsIgnoreCase(keyword))
            );
        }

        if (requestDto.getRole() != null) {
            builder.and(user.roleName.eq(requestDto.getRole()));
        }

        List<User> content = queryFactory
            .selectFrom(user)
            .where(builder)
            .offset(requestDto.getPageable().getOffset())
            .limit(requestDto.getPageable().getPageSize())
            .orderBy(user.createdAt.desc())
            .fetch();

        long total = queryFactory
            .select(user.count())
            .from(user)
            .where(builder)
            .fetchOne();

        return new PageImpl<>(content, requestDto.getPageable(), total);
    }

    // ✅ 관리자 고급 필터 검색
    @Override
    public Page<User> searchByCondition(UserSearchCondition condition, Pageable pageable) {
        QUser user = QUser.user;
        BooleanBuilder builder = new BooleanBuilder();

        if (condition.getUsername() != null && !condition.getUsername().isBlank()) {
            builder.and(user.username.containsIgnoreCase(condition.getUsername()));
        }

        if (condition.getEmail() != null && !condition.getEmail().isBlank()) {
            builder.and(user.email.containsIgnoreCase(condition.getEmail()));
        }

        if (condition.getNickname() != null && !condition.getNickname().isBlank()) {
            builder.and(user.nickname.containsIgnoreCase(condition.getNickname()));
        }

        if (condition.getRole() != null) {
            builder.and(user.roleName.eq(condition.getRole()));
        }

        List<User> content = queryFactory
            .selectFrom(user)
            .where(builder)
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .orderBy(user.createdAt.desc())
            .fetch();

        long total = queryFactory
            .select(user.count())
            .from(user)
            .where(builder)
            .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }
}
