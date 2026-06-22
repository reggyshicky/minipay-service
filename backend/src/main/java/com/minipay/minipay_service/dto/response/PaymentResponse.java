package com.minipay.minipay_service.dto.response;

import com.minipay.minipay_service.domain.enums.PaymentMethod;
import com.minipay.minipay_service.domain.enums.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentResponse {
    private UUID id;
    private BigDecimal amount;
    private String phoneNumber;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String reference;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
