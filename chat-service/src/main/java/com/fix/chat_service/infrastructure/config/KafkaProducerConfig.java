package com.fix.chat_service.infrastructure.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fix.chat_service.application.dtos.ChatMessage;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.producer.acks}")
    private String acks;

    @Value("${spring.kafka.producer.retries}")
    private Integer retries;

    @Value("${spring.kafka.producer.batch-size}")
    private Integer batchSize;

    @Value("${spring.kafka.producer.linger-ms}")
    private Integer lingerMs;

    @Value("${spring.kafka.producer.request-timeout}")
    private Integer requestTimeout;


    private final ObjectMapper objectMapper;

    @Bean
    public ProducerFactory<String, ChatMessage> producerConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        props.put(ProducerConfig.ACKS_CONFIG, acks); // 모든 리더-팔로워에 반영 시 ack
        props.put(ProducerConfig.RETRIES_CONFIG, retries);  // 재시도 횟수
        props.put(ProducerConfig.LINGER_MS_CONFIG, lingerMs); // batching 지연시간
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSize); // 배치 사이즈
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, requestTimeout); // 요청 타임아웃

        JsonSerializer<ChatMessage> valueSerializer = new JsonSerializer<>(objectMapper);
        return new DefaultKafkaProducerFactory<>(props, new StringSerializer(), valueSerializer);
    }

    @Bean
    public KafkaTemplate<String, ChatMessage> customKafkaTemplate() {
        return new KafkaTemplate<>(producerConfig());
    }

}
