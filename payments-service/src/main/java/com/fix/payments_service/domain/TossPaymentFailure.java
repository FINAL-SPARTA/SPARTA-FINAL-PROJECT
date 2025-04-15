package com.fix.payments_service.domain;

import com.fix.common_service.entity.Basic;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TossPaymentFailure extends Basic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderId;
    private String paymentKey;
    private String errorCode;
    private String errorMessage;
}
