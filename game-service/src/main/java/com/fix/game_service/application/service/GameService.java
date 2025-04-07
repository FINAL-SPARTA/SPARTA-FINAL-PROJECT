package com.fix.game_service.application.service;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;

import com.fix.game_service.application.dtos.request.GameCreateRequest;
import com.fix.game_service.application.dtos.request.GameSearchRequest;
import com.fix.game_service.application.dtos.response.GameCreateResponse;
import com.fix.game_service.application.dtos.response.GameGetOneResponse;
import com.fix.game_service.application.dtos.response.GameListResponse;
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

	private Game findGame(UUID gameId) {
		return gameRepository.findById(gameId)
			.orElseThrow(() -> new GameException(GameException.GameErrorType.GAME_NOT_FOUND));
	}

}