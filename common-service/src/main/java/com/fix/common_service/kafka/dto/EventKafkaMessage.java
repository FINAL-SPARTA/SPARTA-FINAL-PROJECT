package com.fix.common_service.kafka.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventKafkaMessage<T> {
    private String eventType;
    private T payload;
}
