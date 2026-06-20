package com.minipay.minipay_service.repository;

import com.minipay.minipay_service.domain.Payment;
import com.minipay.minipay_service.domain.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByReference(String reference);
    Page<Payment> findByUserId(UUID userId, Pageable pageable);
    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);

    @Query("SELECT p FROM Payment p JOIN FETCH p.user WHERE p.id = :id")
    Optional<Payment> findByIdWithUser(@Param("id") UUID id);
}
