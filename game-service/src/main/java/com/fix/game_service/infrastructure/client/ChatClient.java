package com.fix.game_service.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.fix.game_service.infrastructure.client.dto.ChatCreateRequest;

@FeignClient(name = "chat-service")
public interface ChatClient {

	@PostMapping("/api/v1/chats")
	void createChatRoom(@RequestBody ChatCreateRequest dto);

}
