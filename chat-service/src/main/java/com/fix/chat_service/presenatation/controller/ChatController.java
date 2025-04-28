package com.fix.chat_service.presenatation.controller;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fix.chat_service.application.aop.ValidateUser;
import com.fix.chat_service.application.dtos.request.ChatCreateRequest;
import com.fix.chat_service.application.dtos.request.ChatRoomUpdateRequest;
import com.fix.chat_service.application.dtos.request.ChatRoomUpdateStatusRequest;
import com.fix.chat_service.application.dtos.response.ChatRoomGetOneResponse;
import com.fix.chat_service.application.dtos.response.ChatRoomListResponse;
import com.fix.chat_service.application.dtos.response.ChatRoomStatusUpdateResponse;
import com.fix.chat_service.application.dtos.response.ChatRoomUpdateResponse;
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

	@GetMapping("/{chatId}")
	public ResponseEntity<ChatRoomGetOneResponse> getChatRoom(@PathVariable UUID chatId) {
		ResponseEntity<ChatRoomGetOneResponse> response = chatService.getChatRoom(chatId);
		return response;
	}

	@GetMapping
	public ResponseEntity<PagedModel<ChatRoomListResponse>> getAllChatRooms(
		Pageable pageable
	) {
		PagedModel<ChatRoomListResponse> response = chatService.getAllChatRooms(pageable);
		return ResponseEntity.ok(response);
	}

	@ValidateUser(roles = {"MASTER", "MANAGER"})
	@PutMapping("/{chatId}")
	public ResponseEntity<ChatRoomUpdateResponse> updateChatRoom(
		@PathVariable UUID chatId, @RequestBody ChatRoomUpdateRequest request) {
		ChatRoomUpdateResponse response = chatService.updateChatRoom(chatId, request);
		return ResponseEntity.ok(response);
	}

	@ValidateUser(roles = {"MASTER", "MANAGER"})
	@PatchMapping("/{chatId}/status")
	public ResponseEntity<ChatRoomStatusUpdateResponse> updateChatRoomStatus(
		@PathVariable UUID chatId, @RequestBody ChatRoomUpdateStatusRequest request) {
		ChatRoomStatusUpdateResponse response = chatService.updateChatRoomStatus(chatId, request);
		return ResponseEntity.ok(response);
	}

	@ValidateUser(roles = {"MASTER", "MANAGER"})
	@DeleteMapping("/{chatId}")
	public ResponseEntity<String> deleteChatRoom(@PathVariable UUID chatId, @RequestHeader("x-user-id") Long userId) {
		chatService.deleteChatRoom(chatId, userId);
		return ResponseEntity.ok("채팅방 삭제 성공");
	}

}
