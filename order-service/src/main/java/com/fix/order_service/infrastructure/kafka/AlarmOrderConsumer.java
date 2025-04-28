package com.fix.order_service.infrastructure.kafka;

import com.fix.common_service.kafka.dto.AlarmOrderCompletedPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AlarmOrderConsumer {

    @KafkaListener(topics = "${kafka-topics.alarm.game-started}", groupId = "${kafka-consumer-group.order-service}")
    public void consume(AlarmOrderCompletedPayload payload) {
        log.info("[Consumer] 경기 시작 알림 수신 - gameId: {}", payload.getGameId());

        // 👉 여기서 이후 필요한 로직을 추가하면 됩니다.
    }
}
