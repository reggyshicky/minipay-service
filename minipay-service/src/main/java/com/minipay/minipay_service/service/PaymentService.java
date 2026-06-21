package com.minipay.minipay_service.service;

import com.minipay.minipay_service.repository.PaymentRepository;
import com.minipay.minipay_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final PaymentGatewayService paymentGatewayService;
    private final NotificationService notificationService;
    private final PaymentMapper paymentMapper;

}
