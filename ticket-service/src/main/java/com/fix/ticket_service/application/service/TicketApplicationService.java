package com.fix.ticket_service.application.service;

import com.fix.ticket_service.application.dtos.request.*;
import com.fix.ticket_service.application.dtos.response.*;
import com.fix.ticket_service.domain.model.Ticket;
import com.fix.ticket_service.domain.model.TicketStatus;
import com.fix.ticket_service.domain.repository.TicketRepository;
import com.fix.ticket_service.infrastructure.client.GameClient;
import com.fix.ticket_service.infrastructure.client.OrderClient;
import com.fix.ticket_service.infrastructure.client.StadiumClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.RedissonMultiLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketApplicationService {

    private final TicketRepository ticketRepository;
    private final OrderClient orderClient;
    private final GameClient gameClient;
    private final StadiumClient stadiumClient;
    private final RedissonClient redissonClient;

    private static final long LOCK_WAIT_TIME = 5; // 락 대기 시간 (5초)
    private static final long LOCK_LEASE_TIME = 3; // 락 점유 시간 (3초)

    public List<TicketReserveResponseDto> reserveTicket(TicketReserveRequestDto request, Long userId) {
        List<TicketReserveResponseDto> responseDtoList = new ArrayList<>();
        // 1) Redisson 분산락 적용
        List<RLock> locks = request.getSeatIds().stream()
            .map(seatId -> redissonClient.getLock("seat:" + seatId.toString()))
            .toList();
        RLock multiLock = new RedissonMultiLock(locks.toArray(new RLock[0]));

        try {
            boolean acquired = multiLock.tryLock(LOCK_WAIT_TIME, LOCK_LEASE_TIME, TimeUnit.SECONDS);
            if (!acquired) {
                log.warn("좌석 락 획득 실패: userId={}, seatIds={}", userId, request.getSeatIds());
                throw new IllegalArgumentException("다른 사용자가 좌석을 선택 중입니다.");
            }
            log.info("좌석 락 획득 성공: userId={}, seatIds={}", userId, request.getSeatIds());

            // 2) 중복 예매 방지 (DB 검사)
            // TODO: Redis 캐시를 활용한 중복 예매 방지
            List<Ticket> existingTickets =
                ticketRepository.findByGameIdAndSeatIdInAndStatusIn(request.getGameId(),
                    request.getSeatIds(), List.of(TicketStatus.RESERVED, TicketStatus.SOLD));
            if (!existingTickets.isEmpty()) {
                throw new IllegalArgumentException("이미 예약되었거나 판매된 좌석이 포함되어 있습니다");
            }

            // 3) Feign 호출을 통한 좌석 Id의 유효성 검사 및 가격 조회
            SeatPriceListResponseDto seatPriceListResponseDto =
                stadiumClient.getPrices(new SeatPriceRequestDto(request.getSeatIds()));

            Map<UUID, Integer> seatPriceMap = seatPriceListResponseDto.toMap();

            // 4) 티켓 예약 처리 (엔티티 생성 및 리스트에 저장)
            List<Ticket> ticketsToSave = new ArrayList<>();
            for (UUID seatId : request.getSeatIds()) {
                int price = seatPriceMap.get(seatId);

                Ticket ticket = Ticket.create(userId, request.getGameId(), seatId, price);

                ticketsToSave.add(ticket);
                responseDtoList.add(new TicketReserveResponseDto(ticket));
            }
            // 5) 티켓 정보 일괄 저장
            ticketRepository.saveAll(ticketsToSave);

            // 5) Order 서버를 호출하여 주문 생성 및 결제 처리 요청
            // TODO: 이벤트 발행 방식 비동기 처리
            OrderCreateRequestDto orderCreateRequestDto = new OrderCreateRequestDto(responseDtoList);
            orderClient.createOrder(orderCreateRequestDto);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("락 대기 중 인터럽트 발생: userId={}, seatIds={}", userId, request.getSeatIds(), e);
            throw new RuntimeException("락을 획득하는 동안 문제가 발생했습니다.", e);
        } finally {
            if (multiLock.isLocked() && multiLock.isHeldByCurrentThread()) {
                multiLock.unlock();
                log.info("좌석 락 해제: userId={}, seatIds={}", userId, request.getSeatIds());
            }
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

    @Transactional(readOnly = true)
    public PageResponseDto<TicketResponseDto> searchTickets(UUID gameId, Long userId, int page, int size) {
        Page<TicketResponseDto> mappedPage = ticketRepository.searchTickets(gameId, userId, page, size)
            .map(TicketResponseDto::new);

        return new PageResponseDto<>(mappedPage);
    }

    @Transactional(readOnly = true)
    public List<SeatStatusResponseDto> getSeatView(UUID gameId, UUID stadiumId, String section) {
        // 1) Stadium 서버를 호출하여 구역 내 좌석 정보 조회
        SeatInfoListResponseDto seatInfoListResponseDto =
            stadiumClient.getSeatsBySection(stadiumId, section);

        // 2) DB 조회 : 해당 게임의 RESERVED, SOLD 상태의 티켓 목록 조회
        List<UUID> seatIdsInSection = seatInfoListResponseDto.getSeatInfoList().stream()
            .map(SeatInfoResponseDto::getSeatId)
            .toList();
        List<Ticket> tickets =
            ticketRepository.findByGameIdAndSeatIdInAndStatusIn(gameId, seatIdsInSection, List.of(TicketStatus.RESERVED, TicketStatus.SOLD));

        // 3) 데이터 조합 및 상태 계산 (조회 성능을 위해 Map 으로 변환)
        Map<UUID, TicketStatus> SeatStatusMap = tickets.stream()
            .collect(Collectors.toMap(
                Ticket::getSeatId, // Ticket 엔티티에서 직접 가져옴
                Ticket::getStatus, // Ticket 엔티티에서 직접 가져옴
                (existing, replacement) -> existing
            ));

        // 4) SeatInfoResponseDto 와 TicketStatus 를 조합하여 SeatStatusResponseDto 생성
        List<SeatStatusResponseDto> result = seatInfoListResponseDto.getSeatInfoList().stream()
            .map(seatInfo -> {
                TicketStatus bookedStatus = SeatStatusMap.get(seatInfo.getSeatId());
                Boolean availabilityStatus;

                if (bookedStatus == TicketStatus.RESERVED || bookedStatus == TicketStatus.SOLD) {
                    availabilityStatus = false;
                } else {
                    availabilityStatus = true; // Map에 없으면 AVAILABLE
                }

                return new SeatStatusResponseDto(
                    seatInfo.getSeatId(),
                    seatInfo.getSection(),
                    seatInfo.getSeatRow(),
                    seatInfo.getSeatNumber(),
                    availabilityStatus
                );
            })
            .toList();

        return result;
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
