package com.minipay.minipay_service.controller;

import com.minipay.minipay_service.config.AbstractIntegrationTest;
import com.minipay.minipay_service.domain.enums.PaymentMethod;
import com.minipay.minipay_service.dto.request.PaymentRequest;
import com.minipay.minipay_service.dto.request.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

class PaymentControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;
    private String authToken;

    @BeforeEach
    void registerAndLogin() {
        String uniqueUsername = "paymentuser-" + java.util.UUID.randomUUID();

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername(uniqueUsername);
        registerRequest.setEmail(uniqueUsername + "@test.com");
        registerRequest.setPassword("testpass123");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/register", registerRequest, String.class);

        String body = response.getBody();
        authToken = body.split("\"token\":\"")[1].split("\"")[0];
    }
    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        return headers;
    }

    @Test
    void initiatePayment_shouldReturn201_whenAuthenticatedAndValid() {
        PaymentRequest request = new PaymentRequest();
        request.setAmount(new BigDecimal("500"));
        request.setPhoneNumber("0712345678");
        request.setPaymentMethod(PaymentMethod.MPESA);

        HttpEntity<PaymentRequest> entity = new HttpEntity<>(request, authHeaders());

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/payments", entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).contains("phoneNumber");
    }

    @Test
    void initiatePayment_shouldReturn401_whenNoTokenProvided() {
        PaymentRequest request = new PaymentRequest();
        request.setAmount(new BigDecimal("500"));
        request.setPhoneNumber("0712345678");
        request.setPaymentMethod(PaymentMethod.MPESA);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/payments", request, String.class);

        assertThat(response.getStatusCode()).isIn(HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN);
    }

    @Test
    void initiatePayment_shouldMarkFailed_whenAmountExceedsThreshold() {
        PaymentRequest request = new PaymentRequest();
        request.setAmount(new BigDecimal("150000"));
        request.setPhoneNumber("0712345678");
        request.setPaymentMethod(PaymentMethod.CARD);

        HttpEntity<PaymentRequest> entity = new HttpEntity<>(request, authHeaders());

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/payments", entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).contains("FAILED");
    }

    @Test
    void getPaymentHistory_shouldReturnOkAndPagedResults() {
        PaymentRequest request = new PaymentRequest();
        request.setAmount(new BigDecimal("300"));
        request.setPhoneNumber("0712345678");
        request.setPaymentMethod(PaymentMethod.MPESA);
        restTemplate.postForEntity("/api/payments", new HttpEntity<>(request, authHeaders()), String.class);

        HttpEntity<Void> entity = new HttpEntity<>(authHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/payments?page=0&size=10", org.springframework.http.HttpMethod.GET, entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("content");
    }

    @Test
    void webhookPaymentUpdate_shouldUpdateStatus_whenReferenceExists() {
        PaymentRequest request = new PaymentRequest();
        request.setAmount(new BigDecimal("400"));
        request.setPhoneNumber("0712345678");
        request.setPaymentMethod(PaymentMethod.MPESA);

        ResponseEntity<String> initiateResponse = restTemplate.postForEntity(
                "/api/payments", new HttpEntity<>(request, authHeaders()), String.class);

        String reference = initiateResponse.getBody().split("\"reference\":\"")[1].split("\"")[0];

        String webhookBody = """
                {
                  "reference": "%s",
                  "status": "FAILED",
                  "failureReason": "Simulated webhook failure"
                }
                """.formatted(reference);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        HttpEntity<String> webhookEntity = new HttpEntity<>(webhookBody, headers);

        ResponseEntity<String> webhookResponse = restTemplate.postForEntity(
                "/api/webhooks/payment", webhookEntity, String.class);

        assertThat(webhookResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(webhookResponse.getBody()).contains("Simulated webhook failure");
    }
}