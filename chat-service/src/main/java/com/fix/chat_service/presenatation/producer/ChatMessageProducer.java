package com.fix.chat_service.presenatation.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageProducer {

	private final KafkaTemplate<String, String> kafkaTemplate;

	/**
	 * 해당 토픽으로 메시지 수신
	 * @param topic : 토픽명
	 * @param message : 전달할 메시지
	 */
	public void sendMessage(String topic, String message) {
		kafkaTemplate.send(topic, message);
	}
}

