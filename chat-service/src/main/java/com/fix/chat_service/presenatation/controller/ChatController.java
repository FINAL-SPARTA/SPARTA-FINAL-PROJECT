package com.fix.chat_service.presenatation.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.ui.Model;

import com.fix.chat_service.application.dtos.request.ChatCreateRequest;
import com.fix.chat_service.application.dtos.response.ChatRoomGetOneResponse;
import com.fix.chat_service.application.service.ChatService;

import jakarta.ws.rs.Path;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
public class ChatController {

	private final ChatService chatService;

	@PostMapping
	void createChatRoom(@RequestBody ChatCreateRequest request) {
		log.info(request.getGameName());
		chatService.createChatRoom(request);
	}

	@GetMapping("/{chatId}/info")
	public ResponseEntity<ChatRoomGetOneResponse> getGameInfo(@PathVariable UUID chatId) {
		ResponseEntity<ChatRoomGetOneResponse> response = chatService.getChatRoom(chatId);
		return response;
	}

}
