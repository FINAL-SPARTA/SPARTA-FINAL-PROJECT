//package com.fix.ticket_service;
//
//
//import com.fix.ticket_service.application.dtos.request.OrderCreateRequestDto;
//import com.fix.ticket_service.application.dtos.request.TicketInfoRequestDto;
//import com.fix.ticket_service.application.dtos.request.TicketReserveRequestDto;
//import com.fix.ticket_service.application.service.TicketApplicationService;
//import com.fix.ticket_service.domain.model.Ticket;
//import com.fix.ticket_service.domain.model.TicketStatus;
//import com.fix.ticket_service.domain.repository.TicketRepository;
//import com.fix.ticket_service.infrastructure.client.OrderClient;
//import com.fix.ticket_service.infrastructure.client.StadiumClient;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.DynamicPropertyRegistry;
//import org.springframework.test.context.DynamicPropertySource;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.testcontainers.containers.GenericContainer;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//import org.testcontainers.utility.DockerImageName;
//
//import java.util.List;
//import java.util.UUID;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.atomic.AtomicInteger;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.doNothing;
//
//@Testcontainers
//@SpringBootTest
//class TicketApplicationServiceConcurrencyTest {
//
//    @Autowired
//    private TicketApplicationService ticketApplicationService;
//
//    @Autowired
//    private TicketRepository ticketRepository;
//
//    // Feign Client Mocking
//    @MockitoBean
//    private StadiumClient stadiumClient;
//    @MockitoBean
//    private OrderClient orderClient;
//
//    // Testcontainers Redis 설정
//    @Container
//    static final GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis"))
//        .withExposedPorts(6379);
//
//    // Redis 주소를 동적으로 설정
//    @DynamicPropertySource
//    static void redisProperties(DynamicPropertyRegistry registry) {
//        registry.add("spring.redis.host", redis::getHost);
//        registry.add("spring.redis.port", () -> redis.getMappedPort(6379).toString());
//    }
//
//    @BeforeAll
//    static void setUpAll() {
//        redis.start(); // Redis 컨테이너 시작
//    }
//
//    @AfterEach
//    void tearDown() {
//    }
//
//    @Test
//    @DisplayName("동시에 같은 좌석 예약을 시도하면 1명만 성공해야 한다 (분산락 테스트)")
//    void reserveTicket_concurrencyTest_forSameSeat() throws InterruptedException {
//        // GIVEN
//        final int numberOfThreads = 5; // 동시에 시도할 스레드 수
//        final UUID gameId = UUID.randomUUID();
//        final UUID stadiumId = UUID.randomUUID();
//        final UUID contestedSeatId = UUID.randomUUID(); // 경쟁이 발생할 좌석 ID
//        final Long userId = 1L;
//        final int price = 10000;
//
//        // Mock StadiumClient 응답 설정
////        SeatPriceListResponseDto priceResponse = new SeatPriceListResponseDto(
////            List.of(new SeatPriceResponseDto(contestedSeatId, price))
////        );
////        when(stadiumClient.getPrices(any(SeatPriceRequestDto.class))).thenReturn(priceResponse);
//
//        // Mock OrderClient 설정 (void 메서드이므로 특별한 동작 없음)
//        doNothing().when(orderClient).createOrder(any(OrderCreateRequestDto.class));
//
//        // 테스트 실행 준비
//        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
//        CountDownLatch latch = new CountDownLatch(numberOfThreads); // 모든 스레드가 거의 동시에 시작하도록 제어
//        AtomicInteger successCount = new AtomicInteger(0); // 성공 카운트 (스레드 안전)
//        AtomicInteger failCount = new AtomicInteger(0);  // 실패 카운트
//
////        TicketReserveRequestDto request = new TicketReserveRequestDto(gameId, stadiumId, List.of(new TicketInfoRequestDto(contestedSeatId, price)));
//
//        // WHEN
//        for (int i = 0; i < numberOfThreads; i++) {
//            final Long currentUserId = userId + i; // 각 스레드별 다른 유저 ID
//            executorService.submit(() -> {
//                try {
//                    latch.countDown(); // 준비 완료 신호
//                    latch.await(); // 모든 스레드가 준비될 때까지 대기
//
//                    // 실제 예약 메서드 호출
////                    ticketApplicationService.reserveTicket(request, currentUserId);
//                    successCount.incrementAndGet(); // 성공 시 카운트 증가
//                } catch (Exception e) {
//                    // 예약 실패 시 (락 획득 실패 또는 중복 예외 등)
//                    System.out.println("Thread " + Thread.currentThread().getName() + " failed: " + e.getMessage());
//                    failCount.incrementAndGet(); // 실패 시 카운트 증가
//                }
//            });
//        }
//
//        executorService.shutdown();
//        boolean finished = executorService.awaitTermination(1, java.util.concurrent.TimeUnit.MINUTES); // 모든 작업 완료 대기
//
//        // THEN
//        assertThat(finished).isTrue(); // 모든 스레드가 시간 내에 종료되었는지 확인
//        assertThat(successCount.get()).isEqualTo(1); // 정확히 1개의 스레드만 성공해야 함
//        assertThat(failCount.get()).isEqualTo(numberOfThreads - 1); // 나머지 스레드는 실패해야 함
//
//        // DB 상태 확인: 해당 좌석에 대한 티켓이 1개만 생성되었는지 확인
//        List<Ticket> ticketsInDb = ticketRepository.findByGameIdAndSeatIdInAndStatusIn(gameId, List.of(contestedSeatId), List.of(TicketStatus.RESERVED, TicketStatus.SOLD));
//        assertThat(ticketsInDb).hasSize(1); // DB에도 1개의 티켓만 있어야 함
//
//        System.out.println("분산락 테스트 결과: Success=" + successCount.get() + ", Fail=" + failCount.get());
//    }
//}
