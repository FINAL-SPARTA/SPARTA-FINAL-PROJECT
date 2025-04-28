package com.fix.game_service.domain.repository;

import com.fix.game_service.domain.model.GameEvent;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface GameEventRepository extends MongoRepository<GameEvent, String> {
    List<GameEvent> findByStatus(String pending);
}
