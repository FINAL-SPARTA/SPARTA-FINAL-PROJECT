package com.fix.common_service.kafka.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventKafkaMessage {
    private String eventType;
    private Object payload;
}
