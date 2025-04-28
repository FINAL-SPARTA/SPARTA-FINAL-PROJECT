package com.fix.chat_service.application.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import com.fix.common_service.kafka.dto.GameChatPayload;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fix.chat_service.application.dtos.request.ChatCreateRequest;
import com.fix.chat_service.application.dtos.request.ChatRoomUpdateRequest;
import com.fix.chat_service.application.dtos.request.ChatRoomUpdateStatusRequest;
import com.fix.chat_service.application.dtos.response.ChatRoomGetOneResponse;
import com.fix.chat_service.application.dtos.response.ChatRoomListResponse;
import com.fix.chat_service.application.dtos.response.ChatRoomStatusUpdateResponse;
import com.fix.chat_service.application.dtos.response.ChatRoomUpdateResponse;
import com.fix.chat_service.application.exception.ChatException;
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

	public void createChatRoomByGame(GameChatPayload payload) {
		log.info("[Kafka Event] 서비스로 요청 들어옴");
		LocalDateTime time = LocalDateTime.parse(payload.getGameDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm[:ss]"));
		ChatRoom chatRoom = ChatRoom.builder()
				.chatRoomId(payload.getGameId())
				.gameId(payload.getGameId())
				.gameDate(time)
				.gameName(payload.getGameName())
				.chatOpenDate(time.minusMinutes(10))
				.chatCloseDate(time.plusDays(1))
				.gameStatus(payload.getGameStatus())
				.build();

		ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);
		log.info(savedChatRoom.getGameDate().toString());
	}

	/**
	 * 채팅방 정보를 반환하는 메서드
	 * @param chatId : 채팅방 ID
	 * @return : 채팅방 정보 반환
	 */
	public ResponseEntity<ChatRoomGetOneResponse> getChatRoom(UUID chatId) {
		ChatRoom chatRoom = findChatRoom(chatId);
		// 시간 조건 체크
		if (LocalDateTime.now().isBefore(chatRoom.getChatOpenDate())) {
			return ResponseEntity
				.badRequest()
				.body(ChatRoomGetOneResponse.builder().message("아직 채팅방 오픈 전입니다.").build());
		}

		if (LocalDateTime.now().isAfter(chatRoom.getChatCloseDate())) {
			return ResponseEntity
				.badRequest()
				.body(ChatRoomGetOneResponse.builder().message("채팅방 이용 시간이 종료되었습니다.").build());
		}

		ChatRoomGetOneResponse response = ChatRoomGetOneResponse.fromChatRoom(chatRoom);
		return ResponseEntity
			.ok().body(response);
	}

	/**
	 * 채팅방 전체 조회
	 * @param pageable : 페이지네이션 내용
	 * @return : 페이지에 담은 내용 반환
	 */
	public PagedModel<ChatRoomListResponse> getAllChatRooms(Pageable pageable) {
		Page<ChatRoomListResponse> chatRooms = chatRoomRepository.findAll(pageable).map(ChatRoomListResponse::new);

		PagedModel<ChatRoomListResponse> pagedModel = new PagedModel<>(chatRooms);
		return pagedModel;
	}

	/**
	 * 채팅방 내용 수정
	 * @param chatId : 수정할 채팅방 ID
	 * @param request : 수정할 채팅방 내용
	 * @return : 반환
	 */
	@Transactional
	public ChatRoomUpdateResponse updateChatRoom(UUID chatId, ChatRoomUpdateRequest request) {
		ChatRoom chatRoom = findChatRoom(chatId);
		ChatRoom updateChatRoom = request.toChatRoom();

		chatRoom.updateChatRoomInfo(updateChatRoom);

		return ChatRoomUpdateResponse.fromChatRoom(chatRoom);
	}

	/**
	 * 채팅방 (경기) 상태 수정
	 * @param chatId : 채팅방 ID
	 * @param request : 수정할 상태 내용
	 * @return : 수정한 채팅방 내용
	 */
	@Transactional
	public ChatRoomStatusUpdateResponse updateChatRoomStatus(UUID chatId, ChatRoomUpdateStatusRequest request) {
		ChatRoom chatRoom = findChatRoom(chatId);
		ChatRoom updateChatRoomStatus = request.toChatRoom();

		chatRoom.updateChatRoomStatus(updateChatRoomStatus);

		return ChatRoomStatusUpdateResponse.fromChatRoom(chatRoom);
	}

	/**
	 * 채팅방 삭제
	 * @param chatId : 삭제할 채팅방 ID
	 * @param userId : 삭제할 사용자 ID
	 */
	@Transactional
	public void deleteChatRoom(UUID chatId, Long userId) {
		ChatRoom chatRoom = findChatRoom(chatId);
		chatRoom.softDelete(userId);
	}

	/**
	 * 채팅방 찾기
	 * @param chatId : 채팅방 ID
	 * @return : 찾은 채팅방 정보
	 */
	private ChatRoom findChatRoom(UUID chatId) {
		return chatRoomRepository.findById(chatId)
			.orElseThrow(() -> new ChatException(ChatException.ChatErrorType.CHATROOM_NOT_FOUND));
	}
}
