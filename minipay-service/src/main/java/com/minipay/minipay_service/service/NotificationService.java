package com.minipay.minipay_service.service;

import com.minipay.minipay_service.domain.NotificationLog;
import com.minipay.minipay_service.domain.Payment;
import com.minipay.minipay_service.domain.enums.NotificationStatus;
import com.minipay.minipay_service.repository.NotificationLogRepository;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final SmsService smsService;
    private final NotificationLogRepository notificationLogRepository;
    @Async("smsTaskExecutor")
    @Retry(name = "smsNotification")
    public void sendPaymentNotification(Payment payment) {
        String message = buildMessage(payment);
        log.info("Sending async SMS notification for payment: {}", payment.getId());

        SmsService.SmsResult result = smsService.sendSms(payment.getPhoneNumber(), message);

        NotificationLog notificationLog = NotificationLog.builder()
                .payment(payment)
                .phoneNumber(payment.getPhoneNumber())
                .message(message)
                .status(result.success() ? NotificationStatus.SENT : NotificationStatus.FAILED)
                .sentAt(result.success() ? LocalDateTime.now() : null)
                .errorMessage(result.errorMessage())
                .build();

        notificationLogRepository.save(notificationLog);
        log.info("Notification logged with status: {}", notificationLog.getStatus());
    }

    private String buildMessage(Payment payment) {
        return switch (payment.getStatus()) {
            case SUCCESS -> String.format(
                    "MiniPay: Your payment of KES %s was successful. Ref: %s",
                    payment.getAmount(), payment.getReference()
            );
            case FAILED -> String.format(
                    "MiniPay: Your payment of KES %s failed. Reason: %s",
                    payment.getAmount(), payment.getFailureReason()
            );
            default -> String.format(
                    "MiniPay: Your payment of KES %s is being processed.",
                    payment.getAmount()
            );
        };
    }
}