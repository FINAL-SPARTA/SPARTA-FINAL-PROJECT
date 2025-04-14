package com.fix.chat_service.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.fix.chat_service.application.dtos.request.ChatCreateRequest;
import com.fix.chat_service.application.dtos.response.ChatRoomGetOneResponse;
import com.fix.chat_service.domain.model.ChatRoom;
import com.fix.chat_service.infrastructure.repository.ChatRoomRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

	private final ChatRoomRepository chatRoomRepository;

	public void createChatRoom(ChatCreateRequest request) {
		log.info("서비스로 요청 들어옴");
		ChatRoom chatRoom = ChatRoom.builder()
			.chatRoomId(request.getGameId())
			.gameId(request.getGameId())
			.gameDate(request.getGameDate())
			.gameName(request.getGameName())
			.chatOpenDate(request.getGameDate().minusMinutes(10))
			.chatCloseDate(request.getGameDate().plusDays(1))
			.gameStatus(request.getGameStatus())
			.build();

		ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);
		log.info(savedChatRoom.getGameDate().toString());
	}

	/**
	 * 채팅방 정보를 반환하는 메서드
	 * @param chatId : 채팅방 ID
	 * @return : 채팅방 정보 반환
	 */
	public ChatRoomGetOneResponse getChatRoom(UUID chatId) {
		ChatRoom chatRoom = findChatRoom(chatId);
		return ChatRoomGetOneResponse.fromChatRoom(chatRoom);
	}

	/**
	 * 채팅방 찾기
	 * @param chatId : 채팅방 ID
	 * @return :
	 */
	private ChatRoom findChatRoom(UUID chatId) {
		return chatRoomRepository.findById(chatId)
			.orElseThrow();
	}

}
