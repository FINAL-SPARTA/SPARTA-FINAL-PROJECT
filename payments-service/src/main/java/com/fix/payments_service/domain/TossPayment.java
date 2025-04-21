package com.fix.payments_service.domain;

import com.fix.common_service.entity.Basic;
import jakarta.persistence.*;
import jakarta.persistence.GenerationType;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TossPayment extends Basic { // ✅ createdAt/updatedAt 포함됨

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String paymentKey;

    @Column(nullable = false)
    private UUID orderId;

    @Column(nullable = false)
    private int amount; // 결제 금액

    @Enumerated(EnumType.STRING)
    private TossPaymentMethod method;

    @Enumerated(EnumType.STRING)
    private TossPaymentStatus status;

    private String cardCompany;
    private String cardNumber;
    private String receiptUrl;

    private ZonedDateTime approvedAt;

    public void updateStatus(TossPaymentStatus newStatus) {
        this.status = newStatus;
    }
}