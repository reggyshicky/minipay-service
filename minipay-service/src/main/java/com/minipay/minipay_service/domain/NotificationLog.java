package com.minipay.minipay_service.domain;

import com.minipay.minipay_service.domain.enums.NotificationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name="notification_logs")
public class NotificationLog extends BaseEntity{
    @GeneratedValue(strategy= GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="payment_id", nullable=false)
    private Payment payment;

    @Column(name="phone_number", nullable=false, length=20)
    private String phoneNumber;

    @Lob
    @Column(nullable=false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private NotificationStatus status;

    @Column(name="sent_at")
    private LocalDateTime sentAt;

    @Column(name="error_message")
    private String errorMessage;
}
