//package com.fix.payments_service.presantation;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fix.payments_service.domain.TossPayment;
//import com.fix.payments_service.domain.TossPaymentStatus;
//import com.fix.payments_service.domain.repository.TossPaymentRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import javax.crypto.Mac;
//import javax.crypto.spec.SecretKeySpec;
//import java.nio.charset.StandardCharsets;
//import java.util.Base64;
//import java.util.Optional;
//
//@RestController
//@RequiredArgsConstructor
//@Slf4j
//@RequestMapping("/payments")
//public class PaymentWebhookController {
//
//    @Value("${toss.secret-key}")
//    private String tossSecretKey; // application.yml로 분리 권장
//    private final TossPaymentRepository tossPaymentRepository;
//
//    @PostMapping("/webhook")
//    public ResponseEntity<Void> receiveWebhook(@RequestBody String payload,
//                                               @RequestHeader("Toss-Signature") String tossSignature) {
//        try {
//            // ✅ Toss 공식 문서 기준 HMAC SHA-256 서명 검증
//            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
//            SecretKeySpec secretKeySpec = new SecretKeySpec(tossSecretKey.getBytes(), "HmacSHA256");
//            sha256_HMAC.init(secretKeySpec);
//
//            byte[] hashBytes = sha256_HMAC.doFinal(payload.getBytes(StandardCharsets.UTF_8));
//            String calculatedSignature = Base64.getEncoder().encodeToString(hashBytes);
//
//            // ✅ Toss가 보낸 시그니처와 우리가 계산한 시그니처 비교
//            if (!calculatedSignature.equals(tossSignature)) {
//                log.warn("Toss Webhook 시그니처 불일치");
//                return ResponseEntity.status(403).build();
//            }
//
//            // ✅ JSON 파싱
//            ObjectMapper objectMapper = new ObjectMapper();
//            JsonNode root = objectMapper.readTree(payload);
//
//            String eventType = root.path("eventType").asText();
//            JsonNode data = root.path("data");
//
//            String paymentKey = data.path("paymentKey").asText();
//            String orderId = data.path("orderId").asText();
//            String status = data.path("status").asText();
//
//            // ✅ 로그 기록 (기본)
//            log.info("✅ Toss Webhook 수신됨 - eventType: {}, orderId: {}, paymentKey: {}, status: {}",
//                    eventType, orderId, paymentKey, status);
//            // ✅ 상태 보정 처리
//            if ("payment.status.changed".equals(eventType)) {
//                Optional<TossPayment> optionalPayment = tossPaymentRepository.findByPaymentKey(paymentKey);
//
//                TossPaymentStatus currentStatus = TossPaymentStatus.fromKorName(status);
//
//                if (optionalPayment.isEmpty()) {
//                    log.warn("📌 TossPayment 없음 → 신규 생성됨 - orderId: {}", orderId);
//                    TossPayment newPayment = TossPayment.builder()
//                            .paymentKey(paymentKey)
//                            .orderId(orderId)
//                            .status(currentStatus)
//                            .amount(0)
//                            .method(null)
//                            .build();
//                    tossPaymentRepository.save(newPayment);
//
//                } else {
//                    TossPayment payment = optionalPayment.get();
//                    if (!payment.getStatus().equals(currentStatus)) {
//                        log.info("📌 TossPayment 상태 보정 - {} → {}", payment.getStatus(), currentStatus);
//                        payment.updateStatus(currentStatus);
//                    }
//                }
//            }
//
//            return ResponseEntity.ok().build();
//
//        } catch (Exception e) {
//            log.error("Webhook 처리 중 예외 발생", e);
//            return ResponseEntity.status(500).build();
//        }
//    }
//}
