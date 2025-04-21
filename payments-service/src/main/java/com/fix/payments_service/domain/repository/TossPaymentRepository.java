package com.fix.payments_service.domain.repository;

import com.fix.payments_service.domain.TossPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TossPaymentRepository extends JpaRepository<TossPayment, Long> {
    Optional<TossPayment> findByOrderId(UUID orderId);

    Optional<TossPayment> findByPaymentKey(String paymentKey); // ✅ 이 메서드 반드시 필요!
}
