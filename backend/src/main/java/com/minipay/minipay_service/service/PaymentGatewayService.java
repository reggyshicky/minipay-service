package com.minipay.minipay_service.service;

import com.minipay.minipay_service.exception.PaymentProcessingException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
public class PaymentGatewayService {
    @Value("${payment.gateway.failure-threshold}")
    private BigDecimal failureThreshold;

    @Value("${payment.gateway.random-failure-rate}")
    private double randomFailureRate;

    private final Random random = new Random();

    @CircuitBreaker(name = "paymentGateway", fallbackMethod = "fallbackProcessPayment")
    @Retry(name = "paymentGateway")
    public GatewayResult processPayment(BigDecimal amount, String phoneNumber) {
        log.info("Calling mock payment gateway for amount: {}, phone: {}", amount, phoneNumber);

        simulateNetworkLatency();

        // Deterministic business-rule rejection, not a transient gateway issue,
        // so I return a clean failure directly instead of throwing into the
        // circuit breaker/retry machinery (which is meant for transient errors).
        if (amount.compareTo(failureThreshold) >= 0) {
            log.warn("Payment amount {} exceeds failure threshold {}, simulating failure", amount, failureThreshold);
            return new GatewayResult(false, null, "Transaction declined: amount exceeds gateway limit");
        }

        if (random.nextDouble() < randomFailureRate) {
            log.warn("Random gateway failure triggered for amount: {}", amount);
            throw new PaymentProcessingException("Transaction declined: gateway processing error");
        }

        String reference = "MP-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        log.info("Payment processed successfully, reference: {}", reference);
        return new GatewayResult(true, reference, null);
    }

    public GatewayResult fallbackProcessPayment(BigDecimal amount, String phoneNumber, Throwable t) {
        log.error("Circuit breaker fallback triggered for amount: {}, reason: {}", amount, t.getMessage());
        return new GatewayResult(false, null, "Payment gateway temporarily unavailable: " + t.getMessage());
    }
     public void simulateNetworkLatency() {
        try {
            Thread.sleep(200 + random.nextInt(300));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
     }

     public record GatewayResult(boolean success, String reference, String failureReason) {

     }
}
