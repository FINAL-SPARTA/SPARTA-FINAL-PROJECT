package com.fix.order_service.infrastructure.kafka;

import com.fix.common_service.kafka.dto.*;
import com.fix.common_service.kafka.producer.KafkaProducerHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderProducer {

    private final KafkaProducerHelper kafkaProducerHelper;
    private final NewTopic orderSendAlarmUserIds;

    @Value("${kafka-topics.order.created}")
    private String orderCreatedTopic;

    @Value("${kafka-topics.order.creation-failed}")
    private String orderCreationFailedTopic;

    @Value("${kafka-topics.order.completed}")
    private String orderCompletedTopic;

    @Value("${kafka-topics.order.completion-failed}")
    private String orderCompletionFailedTopic;

    @Value("${kafka-topics.order.canceled}")
    private String orderCanceledTopic;

    @Value("${kafka-topics.order.send-alarm-userIds}")
    private String orderSendAlarmUserIdsTopic;

    /**
     * 주문 생성 이벤트 발행
     */
    public void sendOrderCreatedEvent(String orderId, OrderCreatedPayload payload) {
        send(orderCreatedTopic, orderId, "ORDER_CREATED", payload);
    }

    /**
     * 주문 생성 실패 이벤트 발행
     * - TicketReservedPayload를 기반으로 실패 이벤트 생성
     * - 실패 사유(reason) 포함
     * - Kafka 전송은 공통 메서드로 처리
     */
    public void sendOrderCreationFailedEvent(TicketReservedPayload payload, String reason) {
        OrderCreationFailedPayload eventPayload = new OrderCreationFailedPayload(
                payload.getTicketDetails().stream()
                        .map(TicketReservedPayload.TicketDetail::getTicketId)
                        .toList(),
                payload.getUserId(),
                payload.getGameId(),
                reason
        );

        send(orderCreationFailedTopic, null, "ORDER_CREATION_FAILED", eventPayload);
        log.info("📤 [Kafka] 주문 생성 실패 이벤트 발행: topic={}, userId={}, reason={}",
                orderCreationFailedTopic, payload.getUserId(), reason);
    }

    /**
     * 주문 완료 이벤트 발행
     */
    public void sendOrderCompletedEvent(String orderId, OrderCompletedPayload payload) {
        send(orderCompletedTopic, orderId, "ORDER_COMPLETED", payload);
    }

    /**
     * 주문 완료 실패 이벤트 발행
     */
    public void sendOrderCompletionFailedEvent(String orderId, OrderCompletionFailedPayload payload) {
        send(orderCompletionFailedTopic, orderId, "ORDER_COMPLETION_FAILED", payload);
    }

    /**
     * 주문 취소 이벤트 발행
     */
    public void sendOrderCancelledEvent(String orderId, OrderCancelledPayload payload) {
        send(orderCanceledTopic, orderId, "ORDER_CANCELED", payload);
    }

    /**
     * 알람에게 USERIds 반환
     */
    public void orderSendAlarmUserIds(UUID gameId, List<Long> userIds) {
        OrderSendAlarmUserIdsPayload payload = new OrderSendAlarmUserIdsPayload(gameId,userIds);
        send(orderSendAlarmUserIdsTopic,gameId.toString(),"ORDER_SEND_ALARM_USER_IDS",payload);
        log.info("[OrderAlarmProducer] 알람 발행 - topic: {}, gameId: {}, userIds: {}", orderSendAlarmUserIdsTopic, gameId, userIds);
    }




    /**
     * Kafka 공통 전송 처리
     */
    private <T> void send(String topic, String key, String type, T payload) {
        EventKafkaMessage<T> message = new EventKafkaMessage<>(type, payload);
        kafkaProducerHelper.send(topic, key, message);
    }
}
