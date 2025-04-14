package com.fix.common_service.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventKafkaMessage {
    private String eventType;
    private Object payload;
}
