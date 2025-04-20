package com.fix.chat_service.presenatation.producer;

import java.util.concurrent.CompletableFuture;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fix.chat_service.application.dtos.ChatMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageProducer {

	private final KafkaTemplate<String, ChatMessage> kafkaTemplate;
	private final ObjectMapper objectMapper;

	/**
	 * 해당 토픽으로 메시지 수신
	 * @param topic : 토픽명
	 * @param chatMessage : 전달할 메시지
	 */
	public void sendMessage(String topic, ChatMessage chatMessage) throws JsonProcessingException {
		// String message = objectMapper.writeValueAsString(chatMessage);
		// kafkaTemplate.send(topic, chatMessage);
		CompletableFuture<SendResult<String, ChatMessage>> future =
			kafkaTemplate.send(topic, chatMessage);

		future.whenComplete((result, ex) -> {
			if (ex != null) {
				System.err.println("Kafka 전송 실패: " + ex.getMessage());
			} else {
				System.out.println("Kafka 전송 성공: " + result.getRecordMetadata().offset());
			}
		});

	}
}

