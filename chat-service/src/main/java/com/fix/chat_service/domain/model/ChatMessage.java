package com.fix.chat_service.domain.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "chatmessage")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

	@Id
	private String id;
	// @Indexed
	private String chatId;
	private Long userId;
	private String nickname;
	private String message;
	private LocalDateTime time;
	private String type;

}
