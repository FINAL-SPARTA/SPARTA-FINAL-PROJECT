package com.fix.common_service.kafka.consumer;

public interface IdempotencyChecker {
    boolean isNew(String messageKey);

    void markProcessed(String messageKey);
}
