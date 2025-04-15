package com.fix.chat_service.infrastructure.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fix.chat_service.domain.model.ChatRoom;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, UUID> {

}