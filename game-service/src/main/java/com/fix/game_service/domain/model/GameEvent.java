package com.fix.game_service.domain.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@Document(collection = "outbox_game_event")
@NoArgsConstructor
@AllArgsConstructor
public class GameEvent {

    @Id
    private String id;

    private String eventType;

    private UUID aggregateId;

    private String payload;

    private String status;

    private Instant createdAt = Instant.now();

    public void changeStatus(String status) {
        this.status = status;
    }

}
