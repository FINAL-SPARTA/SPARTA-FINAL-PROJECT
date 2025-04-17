package com.fix.common_service.kafka.config;

import com.fix.common_service.kafka.dto.EventKafkaMessage;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class KafkaProducerConfig {

    @Value("${spring.kafka.producer.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        // Key Serializer : 문자열 사용
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        // Value Serializer : JSON 포맷 사용 (EventKafkaMessage 객체 직렬화)
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        // SAGA 관련 설정 : 모든 ISR에 복제될 때까지 대기
        props.put(ProducerConfig.ACKS_CONFIG, "all");
//        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true); // 멱등 프로듀서 사용
//        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120000); // 전송 타임아웃 (2분)
//        props.put(ProducerConfig.RETRIES_CONFIG, 3); // 재시도 횟수
//        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000); // 재시도 대기 시간 (1초)

        return props;
    }

    @Bean
    public ProducerFactory<String, EventKafkaMessage<?>> producerFactory(Map<String, Object> producerConfigs) {
        return new DefaultKafkaProducerFactory<>(producerConfigs);
    }

    @Bean
    public KafkaTemplate<String, EventKafkaMessage<?>>
    kafkaTemplate(ProducerFactory<String, EventKafkaMessage<?>> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

}
