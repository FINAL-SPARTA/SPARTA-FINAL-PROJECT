package com.fix.payments_service.presantation;


import com.fix.payments_service.application.aop.ValidateUser;
import com.fix.payments_service.application.dtos.response.TossPaymentStatusResponse;
import com.fix.payments_service.domain.TossPayment;
import com.fix.payments_service.domain.repository.TossPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentQueryController {

    private final TossPaymentRepository tossPaymentRepository;

    @GetMapping("/{orderId}")
    @ValidateUser(roles = {"ROLE_USER", "ROLE_MANAGER", "ROLE_MASTER"}) // ✅ 권한 체크 AOP
    public ResponseEntity<TossPaymentStatusResponse> getPaymentStatus(
            @PathVariable UUID orderId,
            @RequestHeader("x-user-id") Long userId,
            @RequestHeader("x-user-role") String userRole
    ) {
        TossPayment payment = tossPaymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("결제 정보가 존재하지 않습니다."));

        // 👉 (선택) userId와 매칭 체크 가능: if (!payment.getUserId().equals(userId)) throw ...

        TossPaymentStatusResponse response = TossPaymentStatusResponse.builder()
                .orderId(payment.getOrderId().toString())
                .method(payment.getMethod())
                .status(payment.getStatus())
                .amount(payment.getAmount())
                .approvedAt(payment.getApprovedAt())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/order-info/{orderId}")
    public ResponseEntity<Map<String, Object>> getOrderAmountInfo(@PathVariable UUID orderId) {
        TossPayment payment = tossPaymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("결제 정보가 존재하지 않습니다."));

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", payment.getOrderId());
        result.put("amount", payment.getAmount());

        return ResponseEntity.ok(result);
    }
}
