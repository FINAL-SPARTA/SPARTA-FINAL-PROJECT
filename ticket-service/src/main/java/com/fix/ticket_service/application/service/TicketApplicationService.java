package com.fix.ticket_service.application.service;

import com.fix.ticket_service.application.dtos.request.TicketReserveRequestDto;
import com.fix.ticket_service.application.dtos.response.PageResponseDto;
import com.fix.ticket_service.application.dtos.response.TicketDetailResponseDto;
import com.fix.ticket_service.application.dtos.response.TicketReserveResponseDto;
import com.fix.ticket_service.application.dtos.response.TicketResponseDto;
import com.fix.ticket_service.domain.model.Ticket;
import com.fix.ticket_service.domain.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketApplicationService {

    private final TicketRepository ticketRepository;

    @Transactional
    public List<TicketReserveResponseDto> reserveTicket(TicketReserveRequestDto request, Long userId) {
        List<TicketReserveResponseDto> responseDtoList = new ArrayList<>();
        // 1) TODO: Redis 분산락 적용
        for (UUID seatId : request.getSeatIds()) {
            // 2) TODO: 중복 예매 방지 (DB 검사 or Redis 캐시 검사)
            // ...

            // 3) TODO: Feign 호출을 통한 좌석 Id의 유효성 검사 및 가격 조회
            int price = 10000; // 임시로 설정

            // 4) 티켓 예약 처리 (엔티티 생성 및 DB 저장)
            Ticket ticket = Ticket.create(userId, request.getGameId(), seatId, price);
            ticketRepository.save(ticket);

            responseDtoList.add(new TicketReserveResponseDto(ticket));
        }

        return responseDtoList;
    }

    @Transactional(readOnly = true)
    public TicketDetailResponseDto getTicket(UUID ticketId, Long userId, String userRole) {
        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new IllegalArgumentException("티켓을 찾을 수 없습니다."));

        ticket.validateAuth(userId, userRole);

        return new TicketDetailResponseDto(ticket);
    }

    @Transactional(readOnly = true)
    public PageResponseDto<TicketResponseDto> getTickets(Long userId, int page, int size) {
        Page<TicketResponseDto> mappedPage = ticketRepository.findAllByUserId(userId, page, size)
            .map(TicketResponseDto::new);

        return new PageResponseDto<>(mappedPage);
    }
}
