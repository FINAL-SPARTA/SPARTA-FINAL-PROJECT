package com.fix.game_service.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Repository;

import com.fix.game_service.application.dtos.request.GameSearchRequest;
import com.fix.game_service.application.dtos.response.GameListResponse;
import com.fix.game_service.domain.model.QGame;
import com.fix.game_service.domain.model.Team;
import com.fix.game_service.domain.repository.GameRepositoryCustom;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class GameRepositoryImpl implements GameRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public PagedModel<GameListResponse> searchGame(Pageable pageable, GameSearchRequest request) {
		List<OrderSpecifier<?>> orders = getAllOrderSpecifiers(pageable);

		List<GameListResponse> results = queryFactory
			.select(Projections.fields(
				GameListResponse.class,
				QGame.game.gameId.as("gameId"),
				QGame.game.homeTeam.as("gameTeam1"),
				QGame.game.awayTeam.as("gameTeam2"),
				QGame.game.gameDate.as("gameDate"),
				QGame.game.stadiumId.as("stadiumId")
			))
			.from(QGame.game)
			.where(
				confirmGameTeam1(request.getGameTeam1()),
				confirmGameTeam2(request.getGameTeam2()),
				confirmGameDate(request.getGameDate()),
				confirmStadiumId(request.getStadiumId()),
				QGame.game.isDeleted.eq(false)
			)
			.orderBy(orders.toArray(new OrderSpecifier[0]))
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		Long totalCount = totalCount(request);

		Page<GameListResponse> gameList = new PageImpl<>(results, pageable, totalCount);
		return new PagedModel<>(gameList);
	}

	private Long totalCount(GameSearchRequest request) {
		return queryFactory
			.select(QGame.game.count())
			.from(QGame.game)
			.where(
				confirmGameTeam1(request.getGameTeam1()),
				confirmGameTeam2(request.getGameTeam2()),
				confirmGameDate(request.getGameDate()),
				confirmStadiumId(request.getStadiumId()),
				QGame.game.isDeleted.eq(false)
			)
			.fetchOne();
	}

	/* 검색 조건 */
	private BooleanExpression confirmGameTeam1(Team gameTeam1) {
		return gameTeam1 != null
			? QGame.game.homeTeam.eq(gameTeam1).or(QGame.game.awayTeam.eq(gameTeam1))
			: null;
	}

	private BooleanExpression confirmGameTeam2(Team gameTeam2) {
		return gameTeam2 != null
			? QGame.game.homeTeam.eq(gameTeam2).or(QGame.game.awayTeam.eq(gameTeam2))
			: null;
	}

	private BooleanExpression confirmGameDate(LocalDateTime gameDate) {
		return gameDate != null ? QGame.game.gameDate.eq(gameDate) : null;
	}

	private BooleanExpression confirmStadiumId(Long stadiumId) {
		return stadiumId != null ? QGame.game.stadiumId.eq(stadiumId) : null;
	}

	/* 정렬 */

	/**
	 * 정렬 조건을 기반으로 OrderSpecifier 리스트 생성
	 * @param pageable : 정렬 조건을 포함한 Pageable 객체
	 * @return : QueryDSL에서 사용할 OrderSpecifier 리스트
	 */
	private List<OrderSpecifier<?>> getAllOrderSpecifiers(Pageable pageable) {
		List<OrderSpecifier<?>> orders = new ArrayList<>();

		/* 정렬 기준이 존재한다면
		   pageable -> 클라이언트가 요청한 페이지 정보를 담고 있는 객체, 정렬 정보도 포함
		   sort -> 내부적으로 여러 개의 sort를 가짐 */
		if (pageable.getSort() != null) {
			log.info("정렬 조건이 있어요");
			// 정렬 정보 한 개씩 돌림
			for (Sort.Order sortOrder : pageable.getSort()) {
				orderSorting(sortOrder, orders);
			}
		}

		return orders;
	}

	/**
	 * Sort.Order기반의 정렬 조건을 정렬 리스트에 추가
	 * @param sortOrder : 정렬 정보(오름차순/내림차순)
	 * @param orders : 정렬 조건을 저장할 QueryDSL 리스트
	 */
	private void orderSorting(Sort.Order sortOrder, List<OrderSpecifier<?>> orders) {
		log.info("정렬 기준: " + sortOrder.getProperty() + " / " + "오름차순-내림차순: " + sortOrder.getDirection());
		com.querydsl.core.types.Order direction = sortOrder.isAscending() ? com.querydsl.core.types.Order.ASC : com.querydsl.core.types.Order.DESC;

		// 정렬 기준의 경우에 따라
		switch (sortOrder.getProperty()) {
			case "createdAt" :
				log.info("정렬 조건: createdAt / 오름차순/내림차순" + direction);
				orders.add(new OrderSpecifier<>(direction, QGame.game.createdAt));
				break;
			case "modifiedAt" :
				log.info("정렬 조건: modifiedAt / 오름차순/내림차순" + direction);
				orders.add(new OrderSpecifier<>(direction, QGame.game.updatedAt));
				break;
			default :
				break;
		}
	}
}
