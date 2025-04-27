package com.fix.game_service.domain.repository;

import com.fix.game_service.domain.model.GameEvent;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GameEventRepository extends MongoRepository<GameEvent, String> {
}
