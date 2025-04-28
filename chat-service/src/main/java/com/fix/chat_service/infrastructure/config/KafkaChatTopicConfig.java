package com.fix.chat_service.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaChatTopicConfig {

	@Value("${chat-topic.partitions}")
	private int chatPartitions;

	@Value("${chat-topic.replicas}")
	private int chatReplicas;

	@Value("${kafka-topics.chat.message}")
	private String chatMessageTopic;

	@Bean
	public NewTopic chatMessageTopic() {
		return TopicBuilder.name(chatMessageTopic)
			.partitions(chatPartitions)
			.replicas(chatReplicas)
			.build();
	}
}

