package com.fix.game_service.presentation.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fix.common_service.kafka.dto.GameChatPayload;
import com.fix.common_service.kafka.dto.GameCreatedInfoPayload;
import com.fix.game_service.application.exception.GameException;
import com.fix.game_service.domain.model.GameEvent;
import com.fix.game_service.domain.repository.GameEventRepository;
import com.fix.game_service.presentation.controller.GameProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameEventScheduler {

    private final GameEventRepository gameEventRepository;
    private final GameProducer gameProducer;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 3000)
    public void processGameEventOutbox() {
        List<GameEvent> pendingGameEvents = gameEventRepository.findByStatus("PENDING");

        for (GameEvent event : pendingGameEvents) {
            try {
                // 이벤트 전송
                convertPayload(event.getEventType(), event.getPayload());

                // 성공하면 상태 변경
                event.markAsCompleted();
                gameEventRepository.save(event);

                log.info("Outbox 이벤트 발송 완료: {}", event.getId());
            } catch (Exception e) {
                log.error("Outbox 이벤트 발송 실패: {}", event.getId(), e);
                throw new GameException(GameException.GameErrorType.GAME_PARSING_ERROR);
            }
        }
    }

    private void convertPayload(String eventType, String payload) throws Exception {
        switch (eventType) {
            case "GAME_ALARM_CREATED":
                GameCreatedInfoPayload alarmPayload = objectMapper.readValue(payload, GameCreatedInfoPayload.class);
                gameProducer.sendGameInfoToAlarm(alarmPayload);
                break;

            case "GAME_CHAT_CREATED":
                GameChatPayload chatPayload = objectMapper.readValue(payload, GameChatPayload.class);
                gameProducer.sendGameInfoToChat(chatPayload);
                break;

            default:
                throw new IllegalArgumentException("알 수 없는 이벤트 타입: " + eventType);
        }
    }

}