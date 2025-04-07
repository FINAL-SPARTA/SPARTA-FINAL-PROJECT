package com.fix.game_service.domain.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;

import com.fix.game_service.application.dtos.request.GameSearchRequest;
import com.fix.game_service.application.dtos.response.GameListResponse;

public interface GameRepositoryCustom {

	PagedModel<GameListResponse> searchGame(Pageable pageable, GameSearchRequest request);
}
