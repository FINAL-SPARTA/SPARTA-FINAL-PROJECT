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
    @Value("${kafka-topics.ticket.updated") private String ticketUpdatedTopic;

    // Order 에서 발행하는 이벤트 토픽
    @Value("${kafka-topics.order.created}") private String orderCreatedTopic;
    @Value("${kafka-topics.order.creation-failed}") private String orderCreationFailedTopic; // SAGA

    // payment 에서 발행하는 이벤트 토픽
    // ...

    // Event 에서 발행하는 이벤트 토픽
    @Value("${kafka-topics.event.applied}") private String eventAppliedTopic;

    // User 에서 발행하는 이벤트 토픽
    @Value("${kafka-topics.user.point-deduction-failed}") private String userPointDeductionFailedTopic; // SAGA

    @Value("${spring.kafka.template.default-topic.partitions}") private int defaultPartitions;
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
    public NewTopic ticketUpdated() {
        return TopicBuilder.name(ticketUpdatedTopic)
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
}
