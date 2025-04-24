package com.fix.ticket_service.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class KeyExpiredListener implements MessageListener {

    private final TicketApplicationService ticketApplicationService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        if (!expiredKey.startsWith("reservation:")) {
            return; // 다른 만료 이벤트 무시
        }

        String idPart = expiredKey.substring("reservation:".length());
        try {
            UUID ticketId = UUID.fromString(idPart);
            ticketApplicationService.handleReservationExpiry(ticketId);
        } catch (IllegalArgumentException e) {
            // 잘못된 UUID 포맷 등
            log.error("expired key 에 잘못된 UUID 형식의 티켓 ID가 입력되었습니다: {}", idPart, e);
        }
    }
}
