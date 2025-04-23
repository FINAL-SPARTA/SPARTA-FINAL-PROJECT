package com.fix.ticket_service.application.service;

import com.fix.ticket_service.application.dtos.request.TicketInfoRequestDto;
import com.fix.ticket_service.application.dtos.request.TicketReserveRequestDto;
import com.fix.ticket_service.application.dtos.request.TicketSoldRequestDto;
import com.fix.ticket_service.application.dtos.response.*;
import com.fix.ticket_service.application.exception.TicketException;
import com.fix.ticket_service.domain.model.Ticket;
import com.fix.ticket_service.domain.model.TicketStatus;
import com.fix.ticket_service.domain.repository.TicketRepository;
import com.fix.ticket_service.infrastructure.client.GameClient;
import com.fix.ticket_service.infrastructure.client.OrderClient;
import com.fix.ticket_service.infrastructure.client.StadiumClient;
import com.fix.ticket_service.infrastructure.kafka.producer.TicketProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.RedissonMultiLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
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
    private final TicketProducer ticketProducer;

    private final RedisTemplate<String, String> redisTemplate;

    private static final long LOCK_WAIT_TIME = 5; // 락 대기 시간 (5초)
    private static final long LOCK_LEASE_TIME = 3; // 락 점유 시간 (3초)

    private static final long RESERVED_TTL_SECONDS = 180L; // 예약 상태 TTL (3분)
    private static final long SOLD_TTL_SECONDS = 86400L; // SOLD 상태 TTL (24시간) , 더 길어야 하나? 예매가 얼마나 오래 진행되지..
    private static final String WORKING_QUEUE_KEY_PREFIX = "queue:working:";

    public List<TicketReserveResponseDto> reserveTicket(TicketReserveRequestDto request, Long userId, String token) {
        log.info("티켓 예약 요청: userId={}, gameId={}, seatCount={}", userId, request.getGameId(), request.getSeatInfoList().size());
        // 1) 큐 토큰 검증
        validateQueueToken(token,userId);

        List<TicketReserveResponseDto> responseDtoList = new ArrayList<>();
        List<UUID> seatIds = request.getSeatInfoList().stream()
                .map(TicketInfoRequestDto::getSeatId)
                .toList();
        // 2) Redisson 분산락 적용
        List<RLock> locks = seatIds.stream()
                .map(seatId -> redissonClient.getLock("seat:" + seatId.toString()))
                .toList();
        RLock multiLock = new RedissonMultiLock(locks.toArray(new RLock[0]));

        boolean acquired = false;

        try {
            acquired = multiLock.tryLock(LOCK_WAIT_TIME, LOCK_LEASE_TIME, TimeUnit.SECONDS);
            if (!acquired) {
                log.warn("좌석 락 획득 실패: userId={}, seatInfoList={}", userId, request.getSeatInfoList());
                throw new TicketException(TicketException.TicketErrorType.SEAT_LOCK_ACQUIRE_FAILED);
            }
            log.info("좌석 락 획득 성공: userId={}, seatInfoList={}", userId, request.getSeatInfoList());

            // 3) 중복 예매 방지 (Redis 캐시 검사)
            for (UUID seatId : seatIds) {
                String redisKey = getSeatKey(seatId);
                String status = redisTemplate.opsForValue().get(redisKey);
                if ("RESERVED".equals(status) || "SOLD".equals(status)) {
                    throw new TicketException(TicketException.TicketErrorType.SEAT_ALREADY_RESERVED_OR_SOLD);
                }
            }

            // 4) 중복 예매 방지 (DB 검사) : 이중 방어
            List<Ticket> existingTickets =
                    ticketRepository.findByGameIdAndSeatIdInAndStatusIn(request.getGameId(),
                            seatIds, List.of(TicketStatus.RESERVED, TicketStatus.SOLD));
            if (!existingTickets.isEmpty()) {
                throw new TicketException(TicketException.TicketErrorType.SEAT_ALREADY_RESERVED_OR_SOLD);
            }

            // 5) 티켓 예약 처리 (엔티티 생성 및 리스트에 저장)
            List<Ticket> ticketsToSave = new ArrayList<>();
            for (TicketInfoRequestDto seatInfo : request.getSeatInfoList()) {
                Ticket ticket = Ticket.create(userId, request.getGameId(), seatInfo.getSeatId(), seatInfo.getPrice());
                ticketsToSave.add(ticket);
                responseDtoList.add(new TicketReserveResponseDto(ticket));
            }
            // 6) 티켓 정보 일괄 저장
            ticketRepository.saveAll(ticketsToSave);

            // 7) Redis 캐시에 예약 상태 저장 (TTL = 3분)
            for (UUID seatId : seatIds) {
                String redisKey = getSeatKey(seatId);
                redisTemplate.opsForValue().set(redisKey, "RESERVED", RESERVED_TTL_SECONDS, TimeUnit.SECONDS);
            }

            // 8) 티켓 예매 이벤트 발행 (주문 생성 요청)
            ticketProducer.sendTicketReservedEvent(ticketsToSave, userId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("락 대기 중 인터럽트 발생: userId={}, seatInfoList={}", userId, request.getSeatInfoList(), e);
            throw new TicketException(TicketException.TicketErrorType.SEAT_LOCK_INTERRUPTED);
        } finally {
            if (acquired) {
                multiLock.unlock();
                log.info("좌석 락 해제: userId={}, seatInfoList={}", userId, request.getSeatInfoList());
            }
        }

        return responseDtoList;
    }

    @Transactional(readOnly = true)
    public TicketDetailResponseDto getTicket(UUID ticketId, Long userId, String userRole) {
        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new TicketException(TicketException.TicketErrorType.TICKET_NOT_FOUND));

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
    @Cacheable(value = "seatView", key = "#gameId.toString() + ':' + #stadiumId.toString() + ':' + #section")
    public List<SeatStatusResponseDto> getSeatView(UUID gameId, Long stadiumId, String section) {
        log.info("좌석 뷰 조회 요청: gameId={}, stadiumId={}, section={}", gameId, stadiumId, section);
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
                Ticket::getSeatId,
                Ticket::getStatus,
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
                    seatInfo.getPrice(),
                    availabilityStatus
                );
            })
            .toList();

        log.info("좌석 뷰 조회 성공: gameId={}, stadiumId={}, section={}, resultSize={}", gameId, stadiumId, section, result.size());
        return result;
    }

    @Transactional
    public void updateTicketStatus(TicketSoldRequestDto requestDto) {
        log.info("티켓 상태 업데이트 (SOLD) 시작: orderId={}, ticketIds={}", requestDto.getOrderId(), requestDto.getTicketIds());
        long startTime = System.nanoTime();
        // 1) 입력된 ticketIds 에 해당하는 티켓 목록 조회
        List<Ticket> tickets = ticketRepository.findAllById(requestDto.getTicketIds());

        // 2) 각 티켓 상태 업데이트
        for (Ticket ticket : tickets) {
            ticket.markAsSold(requestDto.getOrderId());
        }

        // 3) 티켓 업데이트 이벤트 발행 (경기 서버에 잔여 좌석 차감 요청)
        int quantity = tickets.size();
        ticketProducer.sendTicketUpdatedEvent(tickets.get(0).getGameId(), -quantity);

        // 4) Redis 캐시에 SOLD 상태 저장 (TTL = 24시간)
        for (Ticket ticket : tickets) {
            String redisKey = getSeatKey(ticket.getSeatId());
            redisTemplate.opsForValue().set(redisKey, "SOLD", SOLD_TTL_SECONDS, TimeUnit.SECONDS);
        }
        long endTime = System.nanoTime();
        log.info("티켓 상태 업데이트 (SOLD) 완료: orderId={}, ticketIds={}, duration={}ms",
            requestDto.getOrderId(), requestDto.getTicketIds(), TimeUnit.NANOSECONDS.toMillis(endTime - startTime));
    }

    @Transactional
    public void cancelTicketStatus(UUID orderId) {
        log.info("티켓 상태 업데이트 (CANCELLED) 시작: orderId={}", orderId);
        long startTime = System.nanoTime();
        // 1) 주문 ID에 해당하는 티켓 목록 조회
        List<Ticket> tickets = ticketRepository.findAllByOrderId(orderId);

        // 2) 각 티켓 상태 업데이트
        for (Ticket ticket : tickets) {
            ticket.markAsCancelled();
        }

        // 3) 티켓 업데이트 이벤트 발행 (경기 서버에 잔여 좌석 증가 요청)
        int quantity = tickets.size();
        ticketProducer.sendTicketUpdatedEvent(tickets.get(0).getGameId(), quantity);

        // 4) Redis 캐시에서 해당 좌석 키 삭제
        for (Ticket ticket : tickets) {
            String redisKey = getSeatKey(ticket.getSeatId());
            redisTemplate.delete(redisKey);
        }
        long endTime = System.nanoTime();
        log.info("티켓 상태 업데이트 (CANCELLED) 완료: orderId={}, ticketIds={}, duration={}ms",
            orderId, tickets.stream().map(Ticket::getTicketId).collect(Collectors.toList()), TimeUnit.NANOSECONDS.toMillis(endTime - startTime));
    }

    @Transactional
    public void deleteTicket(UUID ticketId, Long userId, String userRole) {
        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new TicketException(TicketException.TicketErrorType.TICKET_NOT_FOUND));

        ticket.validateAuth(userId, userRole);

        ticketRepository.delete(ticket);
    }

    @Transactional
    public void deleteTickets(List<UUID> ticketIds) {
        List<Ticket> tickets = ticketRepository.findAllById(ticketIds);

        // 티켓이 존재하지 않거나, 요청한 게임 ID와 일치하지 않는 경우 예외 처리
        if (tickets.isEmpty()) {
            throw new TicketException(TicketException.TicketErrorType.TICKET_NOT_FOUND);
        }

        // 티켓 삭제
        ticketRepository.deleteAll(tickets);
    }

    @Transactional
    public void deleteReservedTickets() {
        ticketRepository.deleteAllByStatus(TicketStatus.RESERVED);
    }

    private String getSeatKey(UUID seatId) {
        return "ticketStatus:" + seatId.toString();
    }

    private void validateQueueToken(String token,Long userId) {
        // 큐 토큰이 null 이거나 비어있는 경우 예외 처리
        if (token == null || token.isEmpty()) {
            throw new TicketException(TicketException.TicketErrorType.QUEUE_TOKEN_REQUIRED);
        }

        // 큐 토큰이 Redis 에 존재하는지 확인 (존재하지 않는다면 TTL 이 만료된 것)
        String redisKey = WORKING_QUEUE_KEY_PREFIX + token + "|" + userId;
        if (!redisTemplate.hasKey(redisKey)) {
            throw new TicketException(TicketException.TicketErrorType.QUEUE_TOKEN_INVALID);
        }

        redisTemplate.delete(redisKey); // 토큰 사용 후 Redis 에서 삭제
    }
}
