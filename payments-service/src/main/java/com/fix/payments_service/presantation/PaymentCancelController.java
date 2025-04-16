package com.fix.payments_service.presantation;

import com.fix.payments_service.application.dtos.request.TossCancelRequestDto;
import com.fix.payments_service.application.dtos.response.TossCancelResponseDto;
import com.fix.payments_service.domain.repository.TossPaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/payments")
public class PaymentCancelController {

    private final TossPaymentRepository tossPaymentRepository;

    @Value("${toss.secret-key}")
    private String secretKey;

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

        TossCancelResponseDto response = responseEntity.getBody();
        log.info("✅ Toss 환불 성공 - paymentKey: {}, status: {}", response.getPaymentKey(), response.getStatus());

        // ✅ TossPayment 상태 업데이트 (보정 처리)
        tossPaymentRepository.findByPaymentKey(paymentKey)
                .ifPresent(payment -> {
                    payment.updateStatus(response.getStatus()); // TossPaymentStatus.CANCELED
                    log.info("✅ TossPayment 상태 보정 완료");
                });

        return ResponseEntity.ok(response);
    }
}

