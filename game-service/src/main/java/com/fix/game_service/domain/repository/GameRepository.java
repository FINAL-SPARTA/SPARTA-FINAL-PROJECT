package com.fix.game_service.domain.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fix.game_service.domain.model.Game;

public interface GameRepository extends JpaRepository<Game, UUID>, GameRepositoryCustom {
}