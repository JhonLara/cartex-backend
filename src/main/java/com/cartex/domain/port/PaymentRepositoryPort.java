package com.cartex.domain.port;

import com.cartex.domain.model.Payment;
import com.cartex.domain.model.PaymentStatus;

import java.util.List;
import java.util.Optional;

public interface PaymentRepositoryPort {

    Payment save(Payment payment);

    Optional<Payment> findById(Long id);

    List<Payment> findByUserId(Long userId);

    List<Payment> findByUserIdAndStatus(Long userId, PaymentStatus status);

    void deleteById(Long id);
}
