package com.minipay.minipay_service.domain;

import com.minipay.minipay_service.domain.enums.PaymentMethod;
import com.minipay.minipay_service.domain.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="payments")
@Builder
public class Payment extends BaseEntity{
    @Id
    @GeneratedValue(strategy= GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id", nullable=false)
    private User user;

    @Column(nullable=false, precision=19, scale=4)
    private BigDecimal amount;

    @Column(name="phone_number", nullable=false, length=20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name="payment_method", nullable=false, length=20)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(unique=true, length=50)
    private String reference;

    @Column(name="failure_reason")
    private String failureReason;
}
