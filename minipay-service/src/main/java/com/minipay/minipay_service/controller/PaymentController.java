package com.minipay.minipay_service.controller;

import com.minipay.minipay_service.dto.request.PaymentRequest;
import com.minipay.minipay_service.dto.response.ApiResponse;
import com.minipay.minipay_service.dto.response.PaymentResponse;
import com.minipay.minipay_service.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> initiatePayment(
            @Valid @RequestBody PaymentRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        PaymentResponse response = paymentService.initiatePayment(request, userDetails.getUsername());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Payment initiated successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(@PathVariable UUID id) {
        PaymentResponse response = paymentService.getPaymentById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Payment retrieved successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> getPaymentHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<PaymentResponse> response = paymentService.getPaymentHistory(userDetails.getUsername(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Payment history retrieved successfully"));
    }
}