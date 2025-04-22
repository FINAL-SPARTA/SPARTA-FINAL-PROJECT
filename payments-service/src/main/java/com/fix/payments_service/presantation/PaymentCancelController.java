package com.fix.payments_service.presantation;

import com.fix.payments_service.application.PaymentEventService;
import com.fix.payments_service.application.dtos.request.TossCancelRequestDto;
import com.fix.payments_service.application.dtos.response.TossCancelResponseDto;
import com.fix.payments_service.domain.repository.TossPaymentRepository;
import com.fix.payments_service.infrastructure.client.OrderServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/payments")
public class PaymentCancelController {

    private final TossPaymentRepository tossPaymentRepository;
    private final PaymentEventService paymentEventService;

    @Value("${toss.secret-key}")
    private String secretKey;
    private final OrderServiceClient orderServiceClient;

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<TossCancelResponseDto> cancelPayment(
            @PathVariable String orderId,
            @RequestParam String paymentKey,
            @RequestBody TossCancelRequestDto cancelRequest
    ) {
        log.info("🔁 Toss 환불 요청 시작 - orderId: {}, reason: {}", orderId, cancelRequest.getCancelReason());

        // ✅ Authorization 헤더 구성
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String encodedAuth = Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + encodedAuth);

        // ✅ Toss 환불 API 요청 전송
        HttpEntity<TossCancelRequestDto> requestEntity = new HttpEntity<>(cancelRequest, headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<TossCancelResponseDto> responseEntity = restTemplate.exchange(
                "https://api.tosspayments.com/v1/payments/" + paymentKey + "/cancel",
                HttpMethod.POST,
                requestEntity,
                TossCancelResponseDto.class
        );

//        NPE 발생 문제 수정
        TossCancelResponseDto response = responseEntity.getBody();
        if (response == null) {
            log.error("❌ Toss 결제 취소 응답이 null입니다 . paymentKey: {}, orderId: {}", paymentKey, orderId);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build(); // 혹은 적절한 메시지와 함께 502
        }
        log.info("✅ Toss 환불 성공 - paymentKey: {}, status: {}", response.getPaymentKey(), response.getStatus());

        // ✅ TossPayment 상태 업데이트 (보정 처리)
        tossPaymentRepository.findByPaymentKey(paymentKey)
                .ifPresent(payment -> {
                    payment.updateStatus(response.getStatus()); // TossPaymentStatus.CANCELED
                    log.info("✅ TossPayment 상태 보정 완료");
                });

        // ✅ Kafka 이벤트 발행
        paymentEventService.sendPaymentCancelled(UUID.fromString(orderId));

        // ✅ 주문 상태 CANCELLED 처리 (order-service 연동)
        try {
            orderServiceClient.cancelOrder(orderId);
            log.info("✅ 주문 상태 CANCELLED 처리 요청 완료 - orderId: {}", orderId);
        } catch (Exception e) {
            log.error("❌ 주문 상태 취소 연동 실패 - orderId: {}, error: {}", orderId, e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
}
//POST /api/v1/payments/{orderId}/cancel?paymentKey={paymentKey}
//        {
//        "cancelReason": "사용자 요청으로 결제 취소"
//        }