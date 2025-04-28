package com.fix.game_service.domain.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fix.game_service.domain.model.GameRate;

public interface GameRateRepository extends JpaRepository<GameRate, UUID> {
}