package com.fix.common_service.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    // Ticket 에서 발행하는 이벤트 토픽
    @Value("${kafka-topics.ticket.reserved}") private String ticketReservedTopic;
    @Value("${kafka-topics.ticket.sold}") private String ticketSoldTopic;
    @Value("${kafka-topics.ticket.cancelled}") private String ticketCancelledTopic;

    // Order 에서 발행하는 이벤트 토픽
    @Value("${kafka-topics.order.created}") private String orderCreatedTopic;
    @Value("${kafka-topics.order.creation-failed}") private String orderCreationFailedTopic; // SAGA
    @Value("${kafka-topics.order.completed}") private String orderCompletedTopic;
    @Value("${kafka-topics.order.completion-failed}") private String orderCompletionFailedTopic; // SAGA
    @Value("${kafka-topics.order.canceled}") private String orderCanceledTopic;

    // payment 에서 발행하는 이벤트 토픽
    @Value("${kafka-topics.payment.completed}") private String paymentCompletedTopic;
    @Value("${kafka-topics.payment.completion-failed}") private String paymentCompletionFailedTopic; // SAGA
    @Value("${kafka-topics.payment.cancelled}") private String paymentCancelledTopic;
    // Event 에서 발행하는 이벤트 토픽
    @Value("${kafka-topics.event.applied}") private String eventAppliedTopic;

    // User 에서 발행하는 이벤트 토픽
    @Value("${kafka-topics.user.point-deduction-failed}") private String userPointDeductionFailedTopic; // SAGA

    // Game 에서 발행하는 이벤트 토픽
    @Value("${kafka-topics.game.created}") private String gameCreatedTopic;

    @Value("${default-topic.partitions}") private int defaultPartitions;
    @Value("${default-topic.replicas}") private int defaultReplicas;

    // 각 토픽에 대한 NewTopic Bean 정의
    // 파티션, 복제본 수 등을 설정
    @Bean
    public NewTopic ticketReserved() {
        return TopicBuilder.name(ticketReservedTopic)
                .partitions(defaultPartitions)
                .replicas(defaultReplicas)
                .build();
    }

    @Bean
    public NewTopic ticketSold() {
        return TopicBuilder.name(ticketSoldTopic)
                .partitions(defaultPartitions)
                .replicas(defaultReplicas)
                .build();
    }

    @Bean
    public NewTopic ticketCancelled() {
        return TopicBuilder.name(ticketCancelledTopic)
                .partitions(defaultPartitions)
                .replicas(defaultReplicas)
                .build();
    }

    @Bean
    public NewTopic orderCreated() {
        return TopicBuilder.name(orderCreatedTopic)
                .partitions(defaultPartitions)
                .replicas(defaultReplicas)
                .build();
    }

    @Bean
    public NewTopic orderCreationFailed() {
        return TopicBuilder.name(orderCreationFailedTopic)
                .partitions(defaultPartitions)
                .replicas(defaultReplicas)
                .build();
    }

    @Bean
    public NewTopic orderCompleted() {
        return TopicBuilder.name(orderCompletedTopic)
                .partitions(defaultPartitions)
                .replicas(defaultReplicas)
                .build();
    }

    @Bean
    public NewTopic orderCompletionFailed() {
        return TopicBuilder.name(orderCompletionFailedTopic)
                .partitions(defaultPartitions)
                .replicas(defaultReplicas)
                .build();
    }

    @Bean
    public NewTopic orderCanceled() {
        return TopicBuilder.name(orderCanceledTopic)
                .partitions(defaultPartitions)
                .replicas(defaultReplicas)
                .build();
    }

    @Bean
    public NewTopic paymentCompleted() {
        return TopicBuilder.name(paymentCompletedTopic)
                .partitions(defaultPartitions)
                .replicas(defaultReplicas)
                .build();
    }

    @Bean
    public NewTopic paymentCompletionFailed() {
        return TopicBuilder.name(paymentCompletionFailedTopic)
                .partitions(defaultPartitions)
                .replicas(defaultReplicas)
                .build();
    }

    @Bean
    public NewTopic paymentCancelled() {
        return TopicBuilder.name(paymentCancelledTopic)
                .partitions(defaultPartitions)
                .replicas(defaultReplicas)
                .build();
    }

    @Bean
    public NewTopic eventApplied() {
        return TopicBuilder.name(eventAppliedTopic)
                .partitions(defaultPartitions)
                .replicas(defaultReplicas)
                .build();
    }

    @Bean
    public NewTopic userPointDeductionFailed() {
        return TopicBuilder.name(userPointDeductionFailedTopic)
                .partitions(defaultPartitions)
                .replicas(defaultReplicas)
                .build();
    }

    @Bean
    public NewTopic gameCreatedTopic() {
        return TopicBuilder.name(gameCreatedTopic)
            .partitions(1)
            .replicas(defaultReplicas)
            .build();
    }

}