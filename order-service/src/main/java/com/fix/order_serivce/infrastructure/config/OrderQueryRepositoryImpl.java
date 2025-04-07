package com.fix.order_serivce.infrastructure.config;

import com.fix.order_serivce.application.dtos.request.OrderSearchCondition;
import com.fix.order_serivce.application.dtos.response.OrderResponse;
import com.fix.order_serivce.domain.repository.OrderQueryRepository;
import com.fix.order_serivce.domain.QOrder;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class OrderQueryRepositoryImpl implements OrderQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<OrderResponse> search(OrderSearchCondition condition, Pageable pageable) {
        QOrder order = QOrder.order;

        BooleanBuilder builder = new BooleanBuilder();

        if (condition.getUserId() != null) {
            builder.and(order.userId.eq(condition.getUserId()));
        }
        if (condition.getGameId() != null) {
            builder.and(order.gameId.eq(condition.getGameId()));
        }
        if (condition.getOrderStatus() != null) {
            builder.and(order.orderStatus.eq(condition.getOrderStatus()));
        }
        if (condition.getFromDate() != null) {
            builder.and(order.createdAt.goe(condition.getFromDate().atStartOfDay()));
        }
        if (condition.getToDate() != null) {
            builder.and(order.createdAt.loe(condition.getToDate().atTime(23, 59, 59)));
        }

        List<OrderResponse> content = queryFactory
                .selectFrom(order)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(order.createdAt.desc()) // 정렬 추가
                .fetch()
                .stream()
                .map(o -> OrderResponse.builder()
                        .orderId(o.getOrderId())
                        .userId(o.getUserId())
                        .gameId(o.getGameId())
                        .peopleCount(o.getPeopleCount())
                        .totalCount(o.getTotalCount())
                        .ticketIds(null) // 간략 응답
                        .build())
                .toList();

        Long total = queryFactory
                .select(order.count())
                .from(order)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }
}
