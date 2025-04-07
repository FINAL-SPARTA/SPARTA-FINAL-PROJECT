package com.fix.game_service.presentation.controller;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fix.common_service.dto.CommonResponse;
import com.fix.common_service.entity.UserRole;
import com.fix.game_service.application.aop.ValidateUser;
import com.fix.game_service.application.dtos.request.GameCreateRequest;
import com.fix.game_service.application.dtos.request.GameSearchRequest;
import com.fix.game_service.application.dtos.response.GameCreateResponse;
import com.fix.game_service.application.dtos.response.GameGetOneResponse;
import com.fix.game_service.application.dtos.response.GameListResponse;
import com.fix.game_service.application.service.GameService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/games")
@RequiredArgsConstructor
public class GameController {

	private final GameService gameService;

	@ValidateUser(roles = {"MASTER", "MANAGER"})
	@PostMapping
	public ResponseEntity<CommonResponse<GameCreateResponse>> createGame(
		@RequestBody GameCreateRequest request,
		@RequestHeader("x-user-role") String role) {
		log.info("Game 측에서 받은 role : {} ", role);
		GameCreateResponse response = gameService.createGame(request);
		return ResponseEntity.ok(CommonResponse.success(
			response, "경기 생성 성공"
		));
	}

	@GetMapping("/{gameId}")
	public ResponseEntity<CommonResponse<GameGetOneResponse>> getOneGame(@PathVariable UUID gameId) {
		GameGetOneResponse response = gameService.getOneGame(gameId);
		return ResponseEntity.ok(CommonResponse.success(
			response, "경기 단건 조회 성공"
		));
	}

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

}