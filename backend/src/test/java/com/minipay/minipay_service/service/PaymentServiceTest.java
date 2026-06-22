package com.minipay.minipay_service.service;

import com.minipay.minipay_service.domain.Payment;
import com.minipay.minipay_service.domain.User;
import com.minipay.minipay_service.domain.enums.PaymentMethod;
import com.minipay.minipay_service.domain.enums.PaymentStatus;
import com.minipay.minipay_service.dto.request.PaymentRequest;
import com.minipay.minipay_service.dto.response.PaymentResponse;
import com.minipay.minipay_service.exception.PaymentNotFoundException;
import com.minipay.minipay_service.mapper.PaymentMapper;
import com.minipay.minipay_service.repository.PaymentRepository;
import com.minipay.minipay_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PaymentGatewayService paymentGatewayService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private PaymentMapper paymentMapper;
    @InjectMocks
    private PaymentService paymentService;

    private User testUser;
    private PaymentRequest paymentRequest;
    private Payment pendingPayment;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .username("ginah")
                .build();

        paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(new BigDecimal("500"));
        paymentRequest.setPhoneNumber("0712345678");
        paymentRequest.setPaymentMethod(PaymentMethod.MPESA);

        pendingPayment = Payment.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .amount(new BigDecimal("500"))
                .phoneNumber("0712345678")
                .paymentMethod(PaymentMethod.MPESA)
                .status(PaymentStatus.PENDING)
                .build();
    }

    @Test
    void initiatePayment_shouldMarkSuccess_whenGatewayReturnsSuccess() {
        when(userRepository.findByUsername("ginah")).thenReturn(Optional.of(testUser));
        when(paymentRepository.save(any(Payment.class))).thenReturn(pendingPayment);
        when(paymentGatewayService.processPayment(any(BigDecimal.class), anyString()))
                .thenReturn(new PaymentGatewayService.GatewayResult(true, "MP-ABC123", null));
        when(paymentMapper.toResponse(any(Payment.class))).thenReturn(
                PaymentResponse.builder().status(PaymentStatus.SUCCESS).build()
        );

        PaymentResponse response = paymentService.initiatePayment(paymentRequest, "ginah");

        assertThat(response.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        verify(paymentRepository, times(2)).save(any(Payment.class));
        verify(notificationService).sendPaymentNotification(any(Payment.class));
    }

    @Test
    void initiatePayment_shouldMarkFailed_whenGatewayReturnsFailure() {
        when(userRepository.findByUsername("ginah")).thenReturn(Optional.of(testUser));
        when(paymentRepository.save(any(Payment.class))).thenReturn(pendingPayment);
        when(paymentGatewayService.processPayment(any(BigDecimal.class), anyString()))
                .thenReturn(new PaymentGatewayService.GatewayResult(false, null, "Gateway declined"));
        when(paymentMapper.toResponse(any(Payment.class))).thenReturn(
                PaymentResponse.builder().status(PaymentStatus.FAILED).failureReason("Gateway declined").build()
        );

        PaymentResponse response = paymentService.initiatePayment(paymentRequest, "ginah");

        assertThat(response.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(response.getFailureReason()).isEqualTo("Gateway declined");
        verify(notificationService).sendPaymentNotification(any(Payment.class));
    }

    @Test
    void initiatePayment_shouldThrowException_whenUserNotFound() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.initiatePayment(paymentRequest, "ghost"))
                .isInstanceOf(UsernameNotFoundException.class);

        verify(paymentRepository, never()).save(any(Payment.class));
        verify(paymentGatewayService, never()).processPayment(any(), any());
    }

    @Test
    void getPaymentById_shouldReturnPayment_whenFound() {
        UUID paymentId = pendingPayment.getId();
        when(paymentRepository.findByIdWithUser(paymentId)).thenReturn(Optional.of(pendingPayment));
        when(paymentMapper.toResponse(pendingPayment)).thenReturn(
                PaymentResponse.builder().id(paymentId).build()
        );

        PaymentResponse response = paymentService.getPaymentById(paymentId);

        assertThat(response.getId()).isEqualTo(paymentId);
    }

    @Test
    void getPaymentById_shouldThrowException_whenNotFound() {
        UUID missingId = UUID.randomUUID();
        when(paymentRepository.findByIdWithUser(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPaymentById(missingId))
                .isInstanceOf(PaymentNotFoundException.class)
                .hasMessageContaining(missingId.toString());
    }

    @Test
    void getPaymentHistory_shouldReturnPagedResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Payment> paymentPage = new PageImpl<>(List.of(pendingPayment));

        when(userRepository.findByUsername("ginah")).thenReturn(Optional.of(testUser));
        when(paymentRepository.findByUserId(testUser.getId(), pageable)).thenReturn(paymentPage);
        when(paymentMapper.toResponse(pendingPayment)).thenReturn(
                PaymentResponse.builder().id(pendingPayment.getId()).build()
        );

        Page<PaymentResponse> result = paymentService.getPaymentHistory("ginah", pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(pendingPayment.getId());
    }
}