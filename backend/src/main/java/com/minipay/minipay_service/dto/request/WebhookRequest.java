package com.minipay.minipay_service.dto.request;

import com.minipay.minipay_service.domain.enums.PaymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WebhookRequest {
    @NotBlank(message="Reference is required")
    private String reference;

    @NotNull(message="Status is required")
    private PaymentStatus status;

    private String failureReason;
}
