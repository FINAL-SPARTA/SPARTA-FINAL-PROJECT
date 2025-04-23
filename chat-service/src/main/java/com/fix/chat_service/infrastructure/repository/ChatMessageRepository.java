package com.fix.chat_service.infrastructure.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.fix.chat_service.domain.model.ChatMessage;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
}
