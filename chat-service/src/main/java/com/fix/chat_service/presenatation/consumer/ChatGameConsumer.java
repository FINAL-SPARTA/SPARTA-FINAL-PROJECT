package com.fix.chat_service.presenatation.consumer;

import com.fix.chat_service.application.service.ChatService;
import com.fix.common_service.kafka.consumer.AbstractKafkaConsumer;
import com.fix.common_service.kafka.consumer.RedisIdempotencyChecker;
import com.fix.common_service.kafka.dto.EventKafkaMessage;
import com.fix.common_service.kafka.dto.GameChatPayload;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ChatGameConsumer extends AbstractKafkaConsumer<GameChatPayload> {

    private final ChatService chatService;

    public ChatGameConsumer(RedisIdempotencyChecker idempotencyChecker, ChatService chatService) {
        super(idempotencyChecker);
        this.chatService = chatService;
    }

    @KafkaListener(topics = "game-chat-topic", groupId = "chat-service-created-consumer")
    public void listenChatRoom(ConsumerRecord<String, EventKafkaMessage<GameChatPayload>> record,
                               EventKafkaMessage<GameChatPayload> message,
                               Acknowledgment acknowledgment) {
        log.info("[Kafka] 채팅방 생성 이벤트 수신 : {}", message.getEventType());
        super.consume(record, message, acknowledgment);
    }

    @Override
    protected void processPayload(Object payload) {
        try {
            GameChatPayload newPayload = mapPayload(payload, GameChatPayload.class);
            log.info("[Kafka] Chat Mapping : {}", newPayload.getGameName());
            chatService.createChatRoomByGame(newPayload);
        } catch (Exception e) {
            log.error("[Kafka] processPayload 실패. Error: {}", e.getMessage(), e);
        }
    }

    @Override
    protected String getConsumerGroupId() {
        return "chat-service-created-consumer";
    }
}
