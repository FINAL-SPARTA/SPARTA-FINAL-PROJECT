package com.fix.payments_service.domain.repository;

import com.fix.payments_service.domain.TossPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TossPaymentRepository extends JpaRepository<TossPayment, Long> {
    Optional<TossPayment> findByOrderId(String orderId);
}
