package com.fix.ticket_service.application.service;

import com.fix.ticket_service.application.dtos.request.OrderCreateRequestDto;
import com.fix.ticket_service.application.dtos.request.SeatPriceRequestDto;
import com.fix.ticket_service.application.dtos.request.TicketReserveRequestDto;
import com.fix.ticket_service.application.dtos.request.TicketSoldRequestDto;
import com.fix.ticket_service.application.dtos.response.*;
import com.fix.ticket_service.domain.model.Ticket;
import com.fix.ticket_service.domain.model.TicketStatus;
import com.fix.ticket_service.domain.repository.TicketRepository;
import com.fix.ticket_service.infrastructure.client.GameClient;
import com.fix.ticket_service.infrastructure.client.OrderClient;
import com.fix.ticket_service.infrastructure.client.StadiumClient;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketApplicationService {

    private final TicketRepository ticketRepository;
    private final OrderClient orderClient;
    private final GameClient gameClient;
    private final StadiumClient stadiumClient;

    @Transactional
    public List<TicketReserveResponseDto> reserveTicket(TicketReserveRequestDto request, Long userId) {
        List<TicketReserveResponseDto> responseDtoList = new ArrayList<>();
        // 1) TODO: Redis 분산락 적용

        // 2) 중복 예매 방지 (DB 검사)
        // TODO: Redis 캐시를 활용한 중복 예매 방지
        List<Ticket> existingTickets =
            ticketRepository.findBySeatIdInAndStatusIn(request.getSeatIds(), List.of(TicketStatus.RESERVED, TicketStatus.SOLD));
        if (!existingTickets.isEmpty()) {
            throw new IllegalArgumentException("이미 예약되었거나 판매된 좌석이 포함되어 있습니다");
        }

        // 3) Feign 호출을 통한 좌석 Id의 유효성 검사 및 가격 조회
        SeatPriceListResponseDto seatPriceListResponseDto =
            stadiumClient.getPrices(new SeatPriceRequestDto(request.getSeatIds()));

        Map<UUID, Integer> seatPriceMap = seatPriceListResponseDto.toMap();

        for (UUID seatId : request.getSeatIds()) {
            int price = seatPriceMap.get(seatId);

            // 4) 티켓 예약 처리 (엔티티 생성 및 DB 저장)
            Ticket ticket = Ticket.create(userId, request.getGameId(), seatId, price);
            ticketRepository.save(ticket);

            responseDtoList.add(new TicketReserveResponseDto(ticket));
        }
        // 5) Order 서버를 호출하여 주문 생성 및 결제 처리 요청
        // TODO: 이벤트 발행 방식 비동기 처리
        OrderCreateRequestDto orderCreateRequestDto = new OrderCreateRequestDto(responseDtoList);
        orderClient.createOrder(orderCreateRequestDto);

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

    @Transactional(readOnly = true)
    public PageResponseDto<TicketResponseDto> searchTickets(UUID gameId, Long userId, int page, int size) {
        Page<TicketResponseDto> mappedPage = ticketRepository.searchTickets(gameId, userId, page, size)
            .map(TicketResponseDto::new);

        return new PageResponseDto<>(mappedPage);
    }

    @Transactional
    public void updateTicketStatus(TicketSoldRequestDto requestDto) {
        // 1) 입력된 ticketIds 에 해당하는 티켓 목록 조회
        List<Ticket> tickets = ticketRepository.findAllById(requestDto.getTicketIds());

        // 2) 각 티켓 상태 업데이트
        for (Ticket ticket : tickets) {
            ticket.markAsSold(requestDto.getOrderId());
        }

        // 3) 경기 서버에 잔여 좌석 업데이트(잔여 좌석 차감) 요청
        // TODO: 이벤트 발행 방식 비동기 처리
        int quantity = tickets.size();
        gameClient.updateRemainingSeats(tickets.get(0).getGameId(), -quantity);
    }

    @Transactional
    public void cancelTicketStatus(UUID orderId) {
        // 1) 주문 ID에 해당하는 티켓 목록 조회
        List<Ticket> tickets = ticketRepository.findAllByOrderId(orderId);

        // 2) 각 티켓 상태 업데이트
        for (Ticket ticket : tickets) {
            ticket.markAsCancelled();
        }

        // 3) 경기 서버에 잔여 좌석 업데이트(잔여 좌석 증가) 요청
        // TODO: 이벤트 발행 방식 비동기 처리
        int quantity = tickets.size();
        gameClient.updateRemainingSeats(tickets.get(0).getGameId(), quantity);
    }

    @Transactional
    public void deleteTicket(UUID ticketId, Long userId, String userRole) {
        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new IllegalArgumentException("티켓을 찾을 수 없습니다."));

        ticket.validateAuth(userId, userRole);

        ticketRepository.delete(ticket);
    }

    @Transactional
    public void deleteReservedTickets() {
        ticketRepository.deleteAllByStatus(TicketStatus.RESERVED);
    }


}
