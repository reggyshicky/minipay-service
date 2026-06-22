package com.minipay.minipay_service.dto.request;

import com.minipay.minipay_service.domain.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    @NotNull(message="Amount is required")
    @DecimalMin(value="1.0", message = "Amount must be at least 1.0")
    private BigDecimal amount;

    @NotBlank(message="Phone number is required")
    @Pattern(
            regexp = "^(?:\\+254|0)[17]\\d{8}$",
            message = "Phone number must be a valid Kenyan number (e.g. 0712345678 or +254712345678"
    )
    private String phoneNumber;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;


}
