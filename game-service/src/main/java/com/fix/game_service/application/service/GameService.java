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
import com.fix.game_service.domain.model.Game;
import com.fix.game_service.domain.repository.GameRepository;
import com.fix.game_service.infrastructure.client.StadiumClient;
import com.fix.game_service.infrastructure.client.dto.StadiumResponseDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {

	private final GameRepository gameRepository;
	private final StadiumClient stadiumClient;

	/**
	 * 경기 생성
	 * @param request : 생성할 경기 내용
	 * @return : 반환
	 */
	public GameCreateResponse createGame(GameCreateRequest request) {
		// 1. Stadium 쪽으로 요청을 전송하여, homeTeam의 경기장 정보를 받아옴
		StadiumResponseDto responseDto = stadiumClient.getStadiumInfo(request.getHomeTeam().toString()).getBody();

		// 2. 받아온 경기장 정보를 기반으로 경기 Entity 생성
		Game game = request.toGame(responseDto.getStadiumId(), responseDto.getStadiumName(), responseDto.getSeatQuantity());

		// 3. 생성한 경기 Entity 저장
		Game savedGame = gameRepository.save(game);

		// 4. 경기 내용 반환
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
		// 1. 수정할 경기 검색
		Game game = findGame(gameId);

		// 2. homeTeam이 변동되었다면 재요청 필요
		Game updateGameInfo;
		if (request.getHomeTeam() != null) {
			StadiumResponseDto response = stadiumClient.getStadiumInfo(request.getHomeTeam().toString()).getBody();
			updateGameInfo = request.toGameWithStadium(response.getStadiumId(), response.getStadiumName(), response.getSeatQuantity());
		} else {
			updateGameInfo = request.toGame();
		}

		// 3. 변동된 사항 저장
		game.updateGame(updateGameInfo);

		// 4. 변동된 경기 반환
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

	/**
	 * 잔여좌석 업데이트
	 * @param gameId : 경기 ID
	 * @param quantity : 뱐동할 잔여 좌석 수량
	 */
	@Transactional
	public void updateGameSeats(UUID gameId, int quantity) {
		Game game = findGame(gameId);

		Integer totalSeats = game.getTotalSeats();
		Integer newRemainingSeats;
		if (game.getRemainingSeats() == null) {
			newRemainingSeats = game.getTotalSeats() - quantity;
		} else {
			newRemainingSeats = game.getRemainingSeats() - quantity;
		}

		Double newAdvanceReservation = Double.valueOf(newRemainingSeats / totalSeats);

		game.updateGameSeats(newRemainingSeats, newAdvanceReservation);
	}

	/**
	 * 경기 삭제
	 * @param gameId : 삭제할 경기 ID
	 * @param userId : 삭제한 사용자의 ID
	 */
	@Transactional
	public void deleteGame(UUID gameId, Long userId) {
		Game game = findGame(gameId);
		game.softDelete(userId);
	}

	private Game findGame(UUID gameId) {
		return gameRepository.findById(gameId)
			.orElseThrow(() -> new GameException(GameException.GameErrorType.GAME_NOT_FOUND));
	}

}