package com.fix.game_service.application.service;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fix.game_service.application.dtos.request.GameCreateRequest;
import com.fix.game_service.application.dtos.request.GameSearchRequest;
import com.fix.game_service.application.dtos.request.GameStatusUpdateRequest;
import com.fix.game_service.application.dtos.request.GameUpdateRequest;
import com.fix.game_service.application.dtos.response.GameCreateResponse;
import com.fix.game_service.application.dtos.response.GameGetOneResponse;
import com.fix.game_service.application.dtos.response.GameListResponse;
import com.fix.game_service.application.dtos.response.GameStatusUpdateResponse;
import com.fix.game_service.application.dtos.response.GameUpdateResponse;
import com.fix.game_service.application.exception.GameException;
import com.fix.game_service.domain.Game;
import com.fix.game_service.domain.repository.GameRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {

	private final GameRepository gameRepository;

	/**
	 * 경기 생성
	 * @param request : 생성할 경기 내용
	 * @return : 반환
	 */
	public GameCreateResponse createGame(GameCreateRequest request) {
		// TODO: Stadium 쪽으로 요청 전송
		// 검증 요청 및 경기장 ID와 총 좌석 수 받아와서 남은 좌석 수에 넣어두기
		Game game = request.toGame();
		Game savedGame = gameRepository.save(game);
		return GameCreateResponse.fromGame(savedGame);
	}

	/**
	 * 경기 단건 조회
	 * @param gameId : 조회할 경기의 ID
	 * @return : 조회한 경기 정보
	 */
	public GameGetOneResponse getOneGame(UUID gameId) {
		Game game = findGame(gameId);
		return GameGetOneResponse.fromGame(game);
	}

	/**
	 * 경기 검색/다건 조회
	 * @param pageable : pagination 적용
	 * @param request : 검색어
	 * @return : 반환 타입
	 */
	public PagedModel<GameListResponse> getAllGames(Pageable pageable, GameSearchRequest request) {
		PagedModel<GameListResponse> gamePage = gameRepository.searchGame(pageable, request);
		return gamePage;
	}

	/**
	 * 경기 수정
	 * @param gameId : 수정할 경기 ID
	 * @param request : 수정할 경기 내용
	 * @return : 반환
	 */
	@Transactional
	public GameUpdateResponse updateGame(UUID gameId, GameUpdateRequest request) {
		Game game = findGame(gameId);
		// TODO : stadiumId != null 이라면 Stadium 쪽으로 검증 요청 필요

		Game updateGameInfo = request.toGame();
		game.updateGame(updateGameInfo);

		return GameUpdateResponse.fromGame(game);
	}

	/**
	 * 경기 상태 수정
	 * @param gameId : 상태를 수정할 경기 ID
	 * @param request : 수정할 경기 상태 내용
	 * @return : 반환
	 */
	@Transactional
	public GameStatusUpdateResponse updateGameStatus(UUID gameId, GameStatusUpdateRequest request) {
		Game game = findGame(gameId);

		Game updateGameStatusInfo = request.toGame();
		game.updateGameStatus(updateGameStatusInfo);

		return GameStatusUpdateResponse.fromGame(game);
	}

	private Game findGame(UUID gameId) {
		return gameRepository.findById(gameId)
			.orElseThrow(() -> new GameException(GameException.GameErrorType.GAME_NOT_FOUND));
	}

}