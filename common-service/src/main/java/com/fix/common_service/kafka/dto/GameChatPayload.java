package com.fix.common_service.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GameChatPayload {

    private UUID gameId;
    private String gameName;
    private String gameDate;
    private String gameStatus;

}
