package com.fix.payments_service.domain.repository;

import com.fix.payments_service.domain.TossPaymentFailure;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TossPaymentFailureRepository extends JpaRepository<TossPaymentFailure, Long> {
}
