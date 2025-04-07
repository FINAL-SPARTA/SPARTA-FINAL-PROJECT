package com.fix.game_service.presentation.controller;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fix.common_service.dto.CommonResponse;
import com.fix.game_service.application.aop.ValidateUser;
import com.fix.game_service.application.dtos.request.GameCreateRequest;
import com.fix.game_service.application.dtos.request.GameSearchRequest;
import com.fix.game_service.application.dtos.request.GameStatusUpdateRequest;
import com.fix.game_service.application.dtos.request.GameUpdateRequest;
import com.fix.game_service.application.dtos.response.GameCreateResponse;
import com.fix.game_service.application.dtos.response.GameGetOneResponse;
import com.fix.game_service.application.dtos.response.GameListResponse;
import com.fix.game_service.application.dtos.response.GameStatusUpdateResponse;
import com.fix.game_service.application.dtos.response.GameUpdateResponse;
import com.fix.game_service.application.service.GameService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/games")
@RequiredArgsConstructor
public class GameController {

	private final GameService gameService;

	/**
	 * 경기 생성
	 * @param request : 생성할 경기 내용
	 * @return : 생성한 경기
	 */
	@ValidateUser(roles = {"MASTER", "MANAGER"})
	@PostMapping
	public ResponseEntity<CommonResponse<GameCreateResponse>> createGame(
		@RequestBody GameCreateRequest request) {
		GameCreateResponse response = gameService.createGame(request);
		return ResponseEntity.ok(CommonResponse.success(
			response, "경기 생성 성공"
		));
	}

	/**
	 * 경기 단건 조회
	 * @param gameId : 단건 조회할 경기 ID
	 * @return : 조회한 경기
	 */
	@GetMapping("/{gameId}")
	public ResponseEntity<CommonResponse<GameGetOneResponse>> getOneGame(@PathVariable UUID gameId) {
		GameGetOneResponse response = gameService.getOneGame(gameId);
		return ResponseEntity.ok(CommonResponse.success(
			response, "경기 단건 조회 성공"
		));
	}

	/**
	 * 경기 전체 조회/검색
	 * @param pageable : pagination을 위한 정보
	 * @param request : 검색 내용
	 * @return : 전체 조회/검색한 경기
	 */
	@GetMapping
	public ResponseEntity<CommonResponse<PagedModel<GameListResponse>>> getAllGames(
		Pageable pageable,
		@ModelAttribute GameSearchRequest request
	) {
		PagedModel<GameListResponse> response = gameService.getAllGames(pageable, request);
		return ResponseEntity.ok(CommonResponse.success(
			response, "경기 다건 조회/검색 성공"
		));
	}

	/**
	 * 경기 수정
	 * @param gameId : 수정할 경기 ID
	 * @param request : 수정할 경기 내용
	 * @return : 수정한 경기
	 */
	@ValidateUser(roles = {"MASTER", "MANAGER"})
	@PutMapping("/{gameId}")
	public ResponseEntity<CommonResponse<GameUpdateResponse>> updateGame(
		@PathVariable UUID gameId,
		@RequestBody GameUpdateRequest request
	)  {
		GameUpdateResponse response = gameService.updateGame(gameId, request);
		return ResponseEntity.ok(CommonResponse.success(
			response, "경기 수정 성공"
		));
	}

	/**
	 * 경기 상태 수정
	 * @param gameId : 상태를 수정할 경기 ID
	 * @param request : 수정할 상태 내용
	 * @return : 상태를 수정한 경기
	 */
	@ValidateUser(roles = {"MASTER", "MANAGER"})
	@PatchMapping("/{gameId}/status")
	public ResponseEntity<CommonResponse<GameStatusUpdateResponse>> updateGameStatus(
		@PathVariable UUID gameId,
		@RequestBody GameStatusUpdateRequest request
	)  {
		GameStatusUpdateResponse response = gameService.updateGameStatus(gameId, request);
		return ResponseEntity.ok(CommonResponse.success(
			response, "경기 상태 수정 성공"
		));
	}

	/**
	 * 경기 삭제
	 * @param gameId : 삭제할 경기 ID
	 * @param userId : 삭제한 사용자 ID
	 * @return : 상태 반환
	 */
	@ValidateUser(roles = {"MASTER", "MANAGER"})
	@DeleteMapping("/{gameId}")
	public ResponseEntity<CommonResponse> deleteGame(
		@PathVariable UUID gameId, @RequestHeader("x-user-id") Long userId) {
		gameService.deleteGame(gameId, userId);
		return ResponseEntity.ok(CommonResponse.success(
			null, "경기 삭제 성공"
		));
	}

}