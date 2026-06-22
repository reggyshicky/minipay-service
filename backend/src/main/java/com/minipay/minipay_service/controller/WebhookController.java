package com.minipay.minipay_service.controller;

import com.minipay.minipay_service.dto.request.WebhookRequest;
import com.minipay.minipay_service.dto.response.ApiResponse;
import com.minipay.minipay_service.dto.response.PaymentResponse;
import com.minipay.minipay_service.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class WebhookController {
    private final PaymentService paymentService;

    @PostMapping("/payment")
    public ResponseEntity<ApiResponse<PaymentResponse>> handlePaymentWebhook(
            @Valid @RequestBody WebhookRequest request
    ) {
        PaymentResponse response = paymentService.updatePaymentFromWebhook(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Webhook processed successfully"));
    }
}