package com.fix.game_service.application.service;

import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fix.game_service.domain.model.GameEvent;
import com.fix.game_service.domain.repository.GameEventRepository;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fix.common_service.dto.StadiumFeignResponse;
import com.fix.common_service.kafka.dto.GameCreatedInfoPayload;
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
import com.fix.game_service.domain.model.GameRate;
import com.fix.game_service.domain.repository.GameRateRepository;
import com.fix.game_service.domain.repository.GameRepository;
import com.fix.game_service.infrastructure.client.ChatClient;
import com.fix.game_service.infrastructure.client.StadiumClient;
import com.fix.game_service.infrastructure.client.dto.ChatCreateRequest;
import com.fix.game_service.presentation.controller.GameProducer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {

	private final CacheManager cacheManager;
	private final GameRepository gameRepository;
	private final GameRateRepository gameRateRepository;
	private final GameEventRepository gameEventRepository;
	private final StadiumClient stadiumClient;
	private final ChatClient chatClient;
	private final GameProducer gameProducer;
	private final ObjectMapper objectMapper;

	/**
	 * 경기 생성
	 * @param request : 생성할 경기 내용
	 * @return : 반환
	 */
	@Transactional
	public GameCreateResponse createGame(GameCreateRequest request) {
		log.info("경기 생성 시작: {}", request);
		// 1. Stadium 쪽으로 요청을 전송하여, homeTeam의 경기장 정보를 받아옴
		log.debug("Stadium 정보 조회 요청 전송: homeTeam={}", request.getHomeTeam());
		StadiumFeignResponse responseDto = getStadiumInfo(request.getHomeTeam().toString());
		log.info("Stadium 정보 조회 완료: stadiumId={}, stadiumName={}, seatQuantity={}",
			responseDto.getStadiumId(), responseDto.getStadiumName(), responseDto.getSeatQuantity());

		// 2. 받아온 경기장 정보를 기반으로 경기 Entity 생성
		Game game = request.toGame(responseDto.getStadiumId(), responseDto.getStadiumName());

		// 3. 생성한 경기 Entity 저장
		Game savedGame = gameRepository.save(game);
		log.info("경기 저장 완료: gameId={}, gameName={}, gameDate={}",
			savedGame.getGameId(), savedGame.getGameName(), savedGame.getGameDate());

		// 4. 경기 예매 기록 Entity 생성
		GameRate gameRate = GameRate.builder().totalSeats(responseDto.getSeatQuantity()).build();

		// 5. 생성한 경기 예매 기록 Entity 저장
		gameRateRepository.save(gameRate);
		savedGame.updateGameRate(gameRate);

		// 6. 경기 내용 alarm으로 전송
		GameCreatedInfoPayload alarmPayload = new GameCreatedInfoPayload(
				savedGame.getGameId(), savedGame.getGameDate(), savedGame.getGameStatus().toString());
		saveGameAlarmEvent(alarmPayload);
		// gameProducer.sendGameInfoToAlarm(alarmPayload);

		// 7. 경기 내용 chat으로 전송
		ChatCreateRequest requestDto = ChatCreateRequest.builder()
			.gameId(savedGame.getGameId()).gameName(savedGame.getGameName())
			.gameDate(savedGame.getGameDate()).gameStatus(savedGame.getGameStatus().toString()).build();
		log.debug("Chat 서비스 채팅방 생성 요청 전송: {}", requestDto);
		chatClient.createChatRoom(requestDto);
		log.info("Chat 서비스 채팅방 생성 요청 완료: gameId={}", savedGame.getGameId());

		// 8. 경기 내용 반환
        log.info("경기 생성 완료: gameId={}, gameName={}", savedGame.getGameId(), savedGame.getGameName());
		return GameCreateResponse.fromGame(savedGame);
	}

	private void saveGameAlarmEvent(GameCreatedInfoPayload alarmPayload) {
		try {
			String payload = objectMapper.writeValueAsString(alarmPayload);

			GameEvent gameEvent = GameEvent.builder()
					.eventType("GAME_CREATED")
					.aggregateId(alarmPayload.getGameId())
					.payload(payload)
					.status("PENDING")
					.build();

			gameEventRepository.save(gameEvent);
		} catch (Exception e) {
			throw new GameException(GameException.GameErrorType.GAME_PARSING_ERROR);
		}
	}

	/**
	 * 경기 단건 조회
	 * @param gameId : 조회할 경기의 ID
	 * @return : 조회한 경기 정보
	 */
	public GameGetOneResponse getOneGame(UUID gameId) {
		Game game = findGame(gameId);
		GameRate gameRate = findGameRate(gameId);
		return GameGetOneResponse.fromGame(game, gameRate);
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
			StadiumFeignResponse response = getStadiumInfo(request.getHomeTeam().toString());
			updateGameInfo = request.toGameWithStadium(response.getStadiumId(), response.getStadiumName());
			GameRate gameRate = findGameRate(gameId);
			gameRate.updateStatus(response.getSeatQuantity());
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
        log.info("경기 잔여 좌석 업데이트 시작: gameId={}, quantity={}", gameId, quantity);
		GameRate gameRate = findGameRate(gameId);

		Integer totalSeats = gameRate.getTotalSeats();
		Integer newRemainingSeats;
		if (gameRate.getRemainingSeats() == null) {
			newRemainingSeats = gameRate.getTotalSeats() + quantity;
		} else {
			newRemainingSeats = gameRate.getRemainingSeats() + quantity;
		}

		Double newAdvanceReservation = (double) (newRemainingSeats / totalSeats);

		gameRate.updateGameSeats(newRemainingSeats, newAdvanceReservation);
        log.info("경기 잔여 좌석 업데이트 완료: gameId={}, newRemainingSeats={}, newAdvanceReservation={}",
            gameId, newRemainingSeats, newAdvanceReservation);
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

	/**
	 * 경기장의 정보를 받아오는 메서드
	 * @param homeTeam : 홈팀의 정보
	 * @return : 경기장 정보 반환
	 */
	private StadiumFeignResponse getStadiumInfo(String homeTeam) {
		Cache cache = cacheManager.getCache("stadiumInfoCache");
		StadiumFeignResponse responseDto = cache.get(homeTeam, StadiumFeignResponse.class);

		if (responseDto == null) {
			responseDto = stadiumClient.getStadiumInfo(homeTeam).getBody();
		}

		return responseDto;
	}

	/**
	 * 경기 찾기
	 * @param gameId : 찾을 경기 ID
	 * @return : 찾은 경기 내용 반환
	 */
	private Game findGame(UUID gameId) {
		return gameRepository.findById(gameId)
			.orElseThrow(() -> new GameException(GameException.GameErrorType.GAME_NOT_FOUND));
	}

	private GameRate findGameRate(UUID gameId) {
		return gameRateRepository.findById(gameId).orElseThrow(() -> new GameException(
			GameException.GameErrorType.GAME_NOT_FOUND));
	}
}