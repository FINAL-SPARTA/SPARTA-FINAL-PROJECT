package com.fix.chat_service.infrastructure.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fix.chat_service.application.dtos.ChatMessage;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class KafkaChatConsumerConfig {

	@Value("${spring.kafka.bootstrap-servers}")
	private String bootstrapServers;

	@Value("${spring.kafka.consumer.properties.spring.json.trusted.packages}")
	private String trustedPackages;

	@Value("${spring.kafka.consumer.group-id}")
	private String groupId;

	@Value("${spring.kafka.consumer.max-poll-records}")
	private Integer maxPollRecords;

	@Value("${spring.kafka.consumer.session-timeout}")
	private Integer sessionTimeout;

	@Value("${spring.kafka.consumer.request-timeout}")
	private Integer requestTimeout;

	@Value("${spring.kafka.consumer.max-poll-interval}")
	private Integer maxPollInterval;

	@Value("${spring.kafka.consumer.concurrency}")
	private Integer concurrency;

	private final ObjectMapper objectMapper;

	/**
	 * Kafka Consumer 기본 설정
	 * @return : 기본 설정 반환
	 */
	@Bean
	public ConsumerFactory<String, ChatMessage> chatConsumerConfig() {
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);  // Kafka 서버 주소
		props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);  // Consumer Group ID
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);  // Key Deserializer

		// Value Deserializer: JSON 사용 + 에러 핸들링 추가
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class); // 에러 핸들링 Deserializer 사용
		props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class); // 실제 사용할 Deserializer 지정

		// JsonDeserializer 설정
		props.put(JsonDeserializer.TRUSTED_PACKAGES, trustedPackages);
		props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, ChatMessage.class.getName()); // 기본 역직렬화 타입
		props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, "true");

		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // 처음 연결 시 가장 오래된 메시지부터 처리
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false"); // 수동 또는 리스너 컨테이너 커밋 사용

		props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords); // 한 번에 가져오는 레코드 수
		props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, sessionTimeout); // 세션 타임아웃
		props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, requestTimeout); // 요청 타임아웃
		props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, maxPollInterval); // 최대 폴링 간격

		JsonDeserializer<ChatMessage> valueDeserializer = new JsonDeserializer<>(ChatMessage.class, objectMapper);

		return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), valueDeserializer);
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, ChatMessage> kafkaChatListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, ChatMessage> factory =
			new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(chatConsumerConfig());
		factory.setConcurrency(concurrency);
		factory.setCommonErrorHandler(new DefaultErrorHandler(new FixedBackOff(0L, 0L)));
		factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
		return factory;
	}

}
