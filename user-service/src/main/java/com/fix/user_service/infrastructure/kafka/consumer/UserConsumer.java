package com.fix.user_service.infrastructure.kafka.consumer;

import com.fix.common_service.dto.EventKafkaMessage;
import com.fix.common_service.dto.EventPayload;
import com.fix.user_service.application.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserConsumer {

    private final UserService userService;

    @KafkaListener(topics = "event-apply-topic", groupId = "user-service")
    public void consumeEventApplyRequest(EventKafkaMessage message) {
        EventPayload payload = message.getPayload();
        log.info("[Kafka] EVENT_APPLY_REQUEST 수신: {}", payload);

        try {
            userService.deductPoints(payload.getUserId(), payload.getPoints());
            log.info("[Kafka] 포인트 차감 성공: userId={}, points={}", payload.getUserId(), payload.getPoints());
        } catch (Exception e) {
            // TODO: 보상 트랜잭션 구현 (차감 실패 이벤트 발행 : 이벤트 응모 취소)
            log.error("[Kafka] 포인트 차감 실패: eventId={}, userId={}, points={}, error={}",
                payload.getEventId(), payload.getUserId(), payload.getPoints(), e.getMessage());
        }
    }
}
