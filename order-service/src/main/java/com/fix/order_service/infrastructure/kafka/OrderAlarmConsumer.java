package com.fix.order_service.infrastructure.kafka;

import com.fix.common_service.kafka.consumer.AbstractKafkaConsumer;
import com.fix.common_service.kafka.consumer.RedisIdempotencyChecker;
import com.fix.common_service.kafka.dto.AlarmGameStartedPayload;
import com.fix.common_service.kafka.dto.EventKafkaMessage;
import com.fix.order_service.application.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderAlarmConsumer extends AbstractKafkaConsumer<AlarmGameStartedPayload> {

    private final OrderService orderService;

    public OrderAlarmConsumer (RedisIdempotencyChecker idempotencyChecker, OrderService orderService) {
        super(idempotencyChecker);
        this.orderService = orderService;
    }
    @KafkaListener(
            topics = "${kafka-topics.alarm.game-started}",
            groupId = "order-service-alarm-game-started-consumer"
    )
    public void listen(ConsumerRecord<String, EventKafkaMessage<AlarmGameStartedPayload>> record,
                       EventKafkaMessage<AlarmGameStartedPayload> message,
                       Acknowledgment ack) {
        super.consume(record, message, ack);
    }
    @Override
    protected void processPayload(Object rawPayload) {
        AlarmGameStartedPayload payload = mapPayload(rawPayload, AlarmGameStartedPayload.class);
        log.info("[Kafka][Order] 알람 서비스로부터 경기 알림 요청 수신 - gameId: {}", payload.getGameId());
        // 핵심 비즈니스 로직 위임
        orderService.findUserIdsByGameId(payload.getGameId());
    }
    @Override
    protected String getConsumerGroupId() {
        return "order-service-alarm-game-started-consumer";
    }
}