package com.fix.game_service.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fix.common_service.kafka.dto.GameChatPayload;
import com.fix.common_service.kafka.dto.GameCreatedInfoPayload;
import com.fix.game_service.presentation.controller.GameProducer;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameEventService {

    @Autowired
    private MongoClient mongoClient;

    private final GameProducer gameProducer;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void watchOutboxTable() {
        MongoCollection<org.bson.Document> collection = mongoClient
                .getDatabase("chatmessages")
                .getCollection("outbox_game_event");

        new Thread(() -> {
            collection.watch().forEach((ChangeStreamDocument<Document> change) -> {
                if ("insert".equals(change.getOperationType().getValue())) {
                    Document doc = change.getFullDocument();

                    if ("PENDING".equalsIgnoreCase(doc.getString("status"))) {
                        try {
                            String eventType = doc.getString("eventType");
                            String payload = doc.getString("payload");
                            convertPayload(eventType, payload);
                            collection.updateOne(
                                    Filters.eq("_id", doc.getObjectId("_id")),
                                    Updates.set("status", "COMPLETED")
                            );

                            log.info("Kafka 이벤트 전송 성공: {}", eventType);

                        } catch (Exception e) {
                            log.error("Kafka 이벤트 전송 실패: {}", e.getMessage());
                        }
                    }
                }
            });
        }).start();
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
