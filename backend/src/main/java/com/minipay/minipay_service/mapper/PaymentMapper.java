package com.minipay.minipay_service.mapper;

import com.minipay.minipay_service.domain.Payment;
import com.minipay.minipay_service.dto.response.PaymentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {
    PaymentResponse toResponse(Payment payment);
}
