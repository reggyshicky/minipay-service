package com.minipay.minipay_service.service;

import com.minipay.minipay_service.domain.Payment;
import com.minipay.minipay_service.domain.User;
import com.minipay.minipay_service.domain.enums.PaymentStatus;
import com.minipay.minipay_service.dto.request.PaymentRequest;
import com.minipay.minipay_service.dto.response.PaymentResponse;
import com.minipay.minipay_service.exception.PaymentNotFoundException;
import com.minipay.minipay_service.mapper.PaymentMapper;
import com.minipay.minipay_service.repository.PaymentRepository;
import com.minipay.minipay_service.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final PaymentGatewayService paymentGatewayService;
    private final NotificationService notificationService;
    private final PaymentMapper paymentMapper;

    @Transactional
    public PaymentResponse initiatePayment(PaymentRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        Payment payment = Payment.builder()
                .user(user)
                .amount(request.getAmount())
                .phoneNumber(request.getPhoneNumber())
                .paymentMethod(request.getPaymentMethod())
                .status(PaymentStatus.PENDING)
                .build();

        payment = paymentRepository.save(payment);
        log.info("Payment created with id: {}, status: PENDING", payment.getId());

        PaymentGatewayService.GatewayResult result = paymentGatewayService.processPayment(
             request.getAmount(), request.getPhoneNumber()
        );

        if (result.success()) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setReference(result.reference());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(result.failureReason());
        }

        payment = paymentRepository.save(payment);
        log.info("Payment {} finalized with status: {}", payment.getId(), payment.getStatus());

        //Firing async SMS - Will not block the response
        notificationService.sendPaymentNotification(payment);

        return paymentMapper.toResponse(payment);
    }

    public PaymentResponse getPaymentById(UUID id) {
        Payment payment = paymentRepository.findByIdWithUser(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id " + id));

        return paymentMapper.toResponse(payment);
    }

    public Page<PaymentResponse> getPaymentHistory(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return paymentRepository.findByUserId(user.getId(), pageable)
                .map(paymentMapper::toResponse);
    }

}
