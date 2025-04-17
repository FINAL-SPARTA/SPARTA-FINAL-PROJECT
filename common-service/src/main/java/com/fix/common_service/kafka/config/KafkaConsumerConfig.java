package com.fix.common_service.kafka.config;

import com.fix.common_service.kafka.dto.EventKafkaMessage;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.consumer.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.properties.spring.json.trusted.packages}")
    private String trustedPackages;

    @Value("${spring.kafka.consumer.group-id}")
    private String defaultGroupId;

    @Value("${spring.kafka.listener.ack-mode}")
    private ContainerProperties.AckMode ackMode;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaConsumerConfig(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }


    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, defaultGroupId); // 기본 그룹 ID
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        // Value Deserializer: JSON 사용 + 에러 핸들링 추가
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class); // 에러 핸들링 Deserializer 사용
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName()); // 실제 사용할 Deserializer 지정
        // JsonDeserializer 설정
        props.put(JsonDeserializer.TRUSTED_PACKAGES, trustedPackages); // 신뢰 패키지 설정 (중요!)
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, EventKafkaMessage.class.getName()); // 기본 역직렬화 타입
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, "false"); // 타입 헤더 사용 안 함

        // Offset 관리 및 커밋 설정
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // 처음 연결 시 가장 오래된 메시지부터
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false"); // 수동 또는 리스너 컨테이너 커밋 사용

        return props;
    }

    @Bean
    public ConsumerFactory<String, EventKafkaMessage<?>> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, EventKafkaMessage<?>> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, EventKafkaMessage<?>> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        // Ack 모드 설정 (application.yml 에서 주입받은 값 사용)
        factory.getContainerProperties().setAckMode(ackMode);
        // factory.setConcurrency(3); // 동시에 실행할 컨슈머 스레드 수 (파티션 수에 맞추거나 조절)

        // === 에러 핸들러 설정 추가 시작 ===

        // 1) DeadLetterPublishingRecoverer 설정: 재시도 소진 시 DLQ로 메시지 발행
        //    - BiFunction을 사용하여 원본 토픽 이름에 ".dlq"를 붙여 DLQ 토픽 이름을 동적으로 생성
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
            (record, exception) -> new TopicPartition(record.topic() + ".dlq", record.partition()));

        // 2) BackOff 설정: 재시도 간격 및 횟수 설정
        //    1초 간격으로 최대 3번 재시도 (총 4번 시도: 원본 1번 + 재시도 3번)
        FixedBackOff backOff = new FixedBackOff(1000L, 3L);

        // 3. DefaultErrorHandler 설정: 위에서 만든 recoverer와 backOff 사용
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);

        // 4. (Option) 특정 예외는 재시도하지 않도록 설정
        // errorHandler.addNotRetryableExceptions(NonRetryableException.class);

        // 5. Listener Container Factory에 Error Handler 설정
        factory.setCommonErrorHandler(errorHandler);

        // === 에러 핸들러 설정 추가 끝 ===

        return factory;
    }
}
