package com.fix.ticket_service.application.service;

import com.fix.common_service.kafka.dto.*;
import com.fix.ticket_service.application.dtos.request.TicketInfoRequestDto;
import com.fix.ticket_service.application.dtos.request.TicketReserveRequestDto;
import com.fix.ticket_service.application.dtos.request.TicketSoldRequestDto;
import com.fix.ticket_service.application.dtos.response.*;
import com.fix.ticket_service.application.exception.TicketException;
import com.fix.ticket_service.domain.model.Ticket;
import com.fix.ticket_service.domain.model.TicketStatus;
import com.fix.ticket_service.domain.repository.TicketRepository;
import com.fix.ticket_service.infrastructure.client.StadiumClient;
import com.fix.ticket_service.infrastructure.kafka.producer.TicketProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.RedissonMultiLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketApplicationService {

    private final TicketRepository ticketRepository;
    private final StadiumClient stadiumClient;
    private final RedissonClient redissonClient;
    private final TicketProducer ticketProducer;

    private final RedisTemplate<String, Object> redisTemplate;

    private static final long LOCK_WAIT_TIME = 5; // 락 대기 시간 (5초)
    private static final long LOCK_LEASE_TIME = 3; // 락 점유 시간 (3초)

    private static final long RESERVED_TTL_SECONDS = 180L; // 예약 상태 TTL (3분)
    private static final long SOLD_TTL_SECONDS = 86400L; // SOLD 상태 TTL (24시간)
    private static final String WORKING_QUEUE_KEY_PREFIX = "queue:working:";
    // Redis 키 정의 (결과 집계용)
    private static final String RESERVATION_AGG_PREFIX = "reservation:agg:";
    private static final String PROCESSED_COUNT_KEY_SUFFIX = ":count";
    private static final String SUCCESS_SEATS_KEY_SUFFIX = ":success"; // Set<ReservedTicketInfo>
    private static final String FAILED_SEATS_KEY_SUFFIX = ":failed";   // Set<FailedSeatInfo>
    private static final Duration AGGREGATION_TTL = Duration.ofMinutes(10); // 집계 데이터 TTL (넉넉하게)

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
                String status = (String) redisTemplate.opsForValue().get(redisKey);
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
            for (Ticket ticket : ticketsToSave) {
                // Redis 캐시 키 생성 (티켓 상태 저장)
                String redisKey = getSeatKey(ticket.getSeatId());
                redisTemplate.opsForValue().set(redisKey, "RESERVED", RESERVED_TTL_SECONDS, TimeUnit.SECONDS);

                // 예약 만료 전용 키
                String reservationKey = "reservation:" + ticket.getTicketId();
                redisTemplate.opsForValue().set(reservationKey, "", RESERVED_TTL_SECONDS, TimeUnit.SECONDS);
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

    public TicketReservationResponseDto initiateAsyncReservation(TicketReserveRequestDto request, Long userId, String queueToken) {
        // 1) 큐 토큰 유효성 검사
        validateQueueToken(queueToken, userId);

        // 2) 고유한 예약 요청 Id 생성
        UUID reservationRequestId = UUID.randomUUID();

        // 3) Kafka 예매 요청 이벤트 발행 (좌석별로) : Producer 호출
        ticketProducer.sendTicketReservationRequestEvent(
            reservationRequestId, userId, queueToken, request.getGameId(), request.getSeatInfoList());
        log.info("티켓 예약 요청 이벤트 발행 완료: reservationRequestId={}, userId={}, gameId={}",
            reservationRequestId, userId, request.getGameId());

        return new TicketReservationResponseDto(reservationRequestId);
    }

    // Kafka Consumer(Worker)가 호출하는 단일 좌석에 대한 티켓 예매 처리 메서드 (비동기)
    @Transactional
    public void processReservation(TicketReservationRequestPayload payload) {
        // 1) Payload 에서 정보 추출
        UUID reservationRequestId = payload.getReservationRequestId();
        Long userId = payload.getUserId();
        UUID gameId = payload.getGameId();
        TicketInfoPayload ticketToProcess = payload.getTicketToProcess();
        UUID seatId = ticketToProcess.getSeatId();
        List<TicketInfoPayload> originalTicketList = payload.getOriginalTicketList();
        int totalTicketsInRequest = payload.getTotalTicketsInRequest();

        // 2) Redisson MultiLock 획득 (원본 요청의 모든 좌석 대상)
        List<RLock> locks = originalTicketList.stream()
            .map(ticketInfo -> redissonClient.getLock("seat:" + ticketInfo.getSeatId().toString()))
            .toList();
        RLock multiLock = new RedissonMultiLock(locks.toArray(new RLock[0]));
        boolean acquired = false;

        try {
            // 3) 락 획득 시도
            acquired = multiLock.tryLock(LOCK_WAIT_TIME, LOCK_LEASE_TIME, TimeUnit.SECONDS);
            if (!acquired) {
                log.warn("좌석 락 획득 실패 (Worker): reqId={}, userId={}, seatId={}, originalSeats:{}",
                    reservationRequestId, userId, seatId, originalTicketList.size());
                recordFailure(reservationRequestId, userId, gameId, ticketToProcess,
                    TicketException.TicketErrorType.SEAT_LOCK_ACQUIRE_FAILED.name(), totalTicketsInRequest, originalTicketList);
                return; // 락 획득 실패 시 빠르게 반환
            }

            // 4) 중복 예매 방지 (Redis 캐시 검사)
            String redisKey = getSeatKey(seatId);
            String status = (String) redisTemplate.opsForValue().get(redisKey);
            if ("RESERVED".equals(status) || "SOLD".equals(status)) {
                throw new TicketException(TicketException.TicketErrorType.SEAT_ALREADY_RESERVED_OR_SOLD);
            }

            // 5) 중복 예매 방지 (DB 검사) : 이중 방어
            List<Ticket> existingTickets = ticketRepository.findByGameIdAndSeatIdInAndStatusIn(
                gameId, List.of(seatId), List.of(TicketStatus.RESERVED, TicketStatus.SOLD));
            if (!existingTickets.isEmpty()) {
                throw new TicketException(TicketException.TicketErrorType.SEAT_ALREADY_RESERVED_OR_SOLD);
            }

            // 6) 티켓 예약 처리 (엔티티 생성 및 저장)
            Ticket ticket = Ticket.create(userId, gameId, seatId, ticketToProcess.getPrice());
            ticketRepository.save(ticket);

            // 7) Redis 캐시에 예약 상태 저장 (TTL = 3분)
            redisTemplate.opsForValue().set(redisKey, "RESERVED", RESERVED_TTL_SECONDS, TimeUnit.SECONDS);
            // 예약 만료 전용 키
            String reservationKey = "reservation:" + ticket.getTicketId();
            redisTemplate.opsForValue().set(reservationKey, "", RESERVED_TTL_SECONDS, TimeUnit.SECONDS);

            // 8) 티켓 예약 성공 처리 및 집계
            recordSuccess(reservationRequestId, userId, gameId, ticket, totalTicketsInRequest, originalTicketList);
        } catch (TicketException e) {
            log.warn("[TicketException] 중복 예매 방지로 인한 좌석 예약 실패 (Worker): reqId={}, seatId={}, error={}",
                reservationRequestId, seatId, e.getMessage());
            recordFailure(reservationRequestId, userId, gameId, ticketToProcess,
                TicketException.TicketErrorType.SEAT_ALREADY_RESERVED_OR_SOLD.name(), totalTicketsInRequest, originalTicketList);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("락 대기 중 인터럽트 발생 (Worker): reqId={}, seatId={}", reservationRequestId, seatId, e);
            recordFailure(reservationRequestId, userId, gameId, ticketToProcess,
                TicketException.TicketErrorType.SEAT_LOCK_INTERRUPTED.name(), totalTicketsInRequest, originalTicketList);
        } finally {
            if (acquired) {
                // 다른 좌석 처리 워커가 아직 락을 사용 중일 수 있음
                // Redisson MultiLock은 모든 락을 한 번에 해제하므로, 여기서 해제하진 않고 점유 시간 만료를 기다림
            }
        }
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
        ticketProducer.sendTicketSoldEvent(tickets.get(0).getGameId(), -quantity);

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
        ticketProducer.sendTicketCancelledEvent(tickets.get(0).getGameId(), quantity);

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
    public void handleReservationExpiry(UUID ticketId) {
        ticketRepository.findById(ticketId).ifPresent(ticket -> {
            if (ticket.getStatus() == TicketStatus.RESERVED) {
                ticketRepository.delete(ticket); // 예약 만료된 티켓 삭제
                log.info("키스페이스 알림에 의해 예약 만료 티켓 삭제: ticketId={}", ticketId);
            }
        });

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

//        redisTemplate.delete(redisKey); // 토큰 사용 후 Redis 에서 삭제
    }

    private void recordSuccess(UUID reservationRequestId, Long userId, UUID gameId,
                               Ticket ticket, int totalTicketsInRequest,
                               List<TicketInfoPayload> originalTicketList) {
        String countKey = RESERVATION_AGG_PREFIX + reservationRequestId.toString() + PROCESSED_COUNT_KEY_SUFFIX;
        String successKey = RESERVATION_AGG_PREFIX + reservationRequestId.toString() + SUCCESS_SEATS_KEY_SUFFIX;
        TicketReservationSucceededPayload.ReservedTicketInfo successInfo =
            new TicketReservationSucceededPayload.ReservedTicketInfo(ticket.getTicketId(), ticket.getSeatId(), ticket.getPrice());

        List<Object> results = redisTemplate.execute(new SessionCallback<List<Object>>() {
            @Override
            public <K, V> List<Object> execute(RedisOperations<K, V> operations) throws DataAccessException {
                operations.multi();
                RedisOperations<String, Object> ops = (RedisOperations<String, Object>) operations;

                ops.opsForSet().add(successKey, successInfo); // 성공 목록에 추가
                ops.expire(successKey, AGGREGATION_TTL); // TTL 설정/갱신
                ops.opsForValue().increment(countKey); // 처리 카운트 증가
                ops.expire(countKey, AGGREGATION_TTL); // TTL 설정/갱신

                return operations.exec();
            }
        });

        long currentCount = ((Number) results.get(2)).longValue();
        log.info("좌석 예약 성공 기록: reqId={}, seatId={}, totalTicketsInRequest={}, currentCount={}",
            reservationRequestId, ticket.getSeatId(), totalTicketsInRequest, currentCount);
        checkAndFinalizeAggregation(reservationRequestId, userId, gameId, currentCount, totalTicketsInRequest, originalTicketList);
    }

    private void recordFailure(UUID reservationRequestId, Long userId, UUID gameId,
                               TicketInfoPayload failedTicketInfo, String reason, int totalTicketsInRequest,
                               List<TicketInfoPayload> originalTicketList) {
        String countKey = RESERVATION_AGG_PREFIX + reservationRequestId.toString() + PROCESSED_COUNT_KEY_SUFFIX;
        String failedKey = RESERVATION_AGG_PREFIX + reservationRequestId.toString() + FAILED_SEATS_KEY_SUFFIX;
        TicketReservationFailedPayload.FailedSeatInfo failedInfo =
            new TicketReservationFailedPayload.FailedSeatInfo(failedTicketInfo.getSeatId(), failedTicketInfo.getPrice(), reason);

        List<Object> results = redisTemplate.execute(new SessionCallback<List<Object>>() {
            @Override
            public <K, V> List<Object> execute(RedisOperations<K, V> operations) throws DataAccessException {
                operations.multi();
                RedisOperations<String, Object> ops = (RedisOperations<String, Object>) operations;

                ops.opsForSet().add(failedKey, failedInfo); // 실패 목록에 추가
                ops.expire(failedKey, AGGREGATION_TTL); // TTL 설정/갱신
                ops.opsForValue().increment(countKey); // 처리 카운트 증가
                ops.expire(countKey, AGGREGATION_TTL); // TTL 설정/갱신

                return operations.exec();
            }
        });

        long currentCount = ((Number) results.get(2)).longValue();
        log.warn("좌석 예약 실패 기록: reqId={}, seatId={}, reason={}, totalTicketsInRequest={}, currentCount={}",
            reservationRequestId, failedTicketInfo.getSeatId(), reason, totalTicketsInRequest, currentCount);
        checkAndFinalizeAggregation(reservationRequestId, userId, gameId, currentCount, totalTicketsInRequest, originalTicketList);
    }

    private void checkAndFinalizeAggregation(UUID reservationRequestId, Long userId, UUID gameId,
                                             long currentCount, int totalTicketsInRequest,
                                             List<TicketInfoPayload> originalTicketList) {
        if (currentCount == totalTicketsInRequest) {
            String successKey = RESERVATION_AGG_PREFIX + reservationRequestId.toString() + SUCCESS_SEATS_KEY_SUFFIX;
            String failedKey = RESERVATION_AGG_PREFIX + reservationRequestId.toString() + FAILED_SEATS_KEY_SUFFIX;
            String countKey = RESERVATION_AGG_PREFIX + reservationRequestId.toString() + PROCESSED_COUNT_KEY_SUFFIX;

            Set<Object> failedSeatsRaw = redisTemplate.opsForSet().members(failedKey);
            Set<TicketReservationFailedPayload.FailedSeatInfo> failedSeats = failedSeatsRaw.stream()
                .map(o -> mapObjectToDto(o, TicketReservationFailedPayload.FailedSeatInfo.class))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());


            Set<Object> successSeatsRaw = redisTemplate.opsForSet().members(successKey);
            Set<TicketReservationSucceededPayload.ReservedTicketInfo> succeededSeats = successSeatsRaw.stream()
                .map(o -> mapObjectToDto(o, TicketReservationSucceededPayload.ReservedTicketInfo.class))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

            if (!failedSeats.isEmpty()) {
                // 최종 실패 처리
                TicketReservationFailedPayload failurePayload = new TicketReservationFailedPayload(
                    reservationRequestId, userId, gameId, new ArrayList<>(failedSeats));
                ticketProducer.sendTicketReservationFailedEvent(failurePayload);

                // 실패 시, 성공했던 좌석들도 취소 처리 (보상 트랜잭션)
                if (!succeededSeats.isEmpty()) {
                    compensateSuccessfulReservations(succeededSeats);
                }
            } else {
                // 최종 성공 처리
                TicketReservationSucceededPayload successPayload = new TicketReservationSucceededPayload(
                    reservationRequestId, userId, gameId, new ArrayList<>(succeededSeats));
                ticketProducer.sendTicketReservationSucceededEvent(successPayload);

                List<Ticket> reservedTickets = succeededSeats.stream()
                    .map(info -> Ticket.create(userId, gameId, info.getSeatId(), info.getPrice()))
                    .toList();
                ticketProducer.sendTicketReservedEvent(reservedTickets, userId);
            }

            // Redis에서 집계 데이터 삭제
            redisTemplate.delete(List.of(successKey, failedKey, countKey));
        } else if (currentCount > totalTicketsInRequest) {
            log.error ("예약 처리 중 카운트 오류: reqId={}, currentCount={}, totalTicketsInRequest={}",
                reservationRequestId, currentCount, totalTicketsInRequest);
        }
    }

    // 티켓 예매 요청 처리 실패 시의 보상 트랜잭션 메서드
    // DB 조회 및 삭제 대신, Redis 캐시에서 예약 상태를 삭제하여,
    // DB 정리는 Keyspace Notifications 만료 리스너에게 위임
    public void compensateSuccessfulReservations(Set<TicketReservationSucceededPayload.ReservedTicketInfo> succeededSeats) {
        List<String> seatStatusKeysToDelete = succeededSeats.stream()
            .map(info -> getSeatKey(info.getSeatId()))
            .toList();
        if (!seatStatusKeysToDelete.isEmpty()) {
            redisTemplate.delete(seatStatusKeysToDelete);
        }
    }

    private <T> T mapObjectToDto(Object obj, Class<T> clazz) {
        if (obj == null) return null;
        try {
            if (obj instanceof Map) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                mapper.findAndRegisterModules();
                return mapper.convertValue(obj, clazz);
            } else if (clazz.isInstance(obj)) {
                return clazz.cast(obj);
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }
}
