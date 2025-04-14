package com.fix.chat_service.presenatation.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fix.chat_service.application.dtos.request.ChatCreateRequest;
import com.fix.chat_service.application.service.ChatService;

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

}
