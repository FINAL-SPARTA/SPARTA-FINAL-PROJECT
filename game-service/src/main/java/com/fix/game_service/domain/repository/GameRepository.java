package com.fix.game_service.domain.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import com.fix.game_service.domain.model.Game;

import jakarta.persistence.LockModeType;

public interface GameRepository extends JpaRepository<Game, UUID>, GameRepositoryCustom {
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Game findByIdWithLock(UUID gameId);
}