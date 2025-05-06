package com.fix.game_service.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Builder
@Document(collection = "outbox_game_event")
@NoArgsConstructor
@AllArgsConstructor
public class GameEvent {

    @Id
    private String id;

    private String eventType;

    private String aggregateId;

    private String payload;

    private String status;

    private Instant createdAt;

}
