package com.cartex.application.usecase;

import com.cartex.application.dto.PaymentRequestDto;
import com.cartex.application.dto.PaymentResponseDto;
import com.cartex.domain.model.Payment;
import com.cartex.domain.model.PaymentStatus;
import com.cartex.domain.model.User;
import com.cartex.domain.port.PaymentRepositoryPort;
import com.cartex.domain.port.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentUseCase {

    private final PaymentRepositoryPort paymentRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;

    public PaymentResponseDto createPayment(PaymentRequestDto request) {
        User user = userRepositoryPort.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));

        Payment payment = Payment.builder()
                .user(user)
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(PaymentStatus.ACTIVE)
                .paymentDate(LocalDateTime.now())
                .expirationDate(LocalDateTime.now().plusMonths(1))
                .build();

        Payment saved = paymentRepositoryPort.save(payment);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentById(Long id) {
        Payment payment = paymentRepositoryPort.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));
        return mapToResponse(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponseDto> getPaymentsByUserId(Long userId) {
        return paymentRepositoryPort.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean hasActivePayment(Long userId) {
        List<Payment> activePayments = paymentRepositoryPort.findByUserIdAndStatus(userId, PaymentStatus.ACTIVE);
        return activePayments.stream().anyMatch(p ->
                p.getExpirationDate() == null || p.getExpirationDate().isAfter(LocalDateTime.now())
        );
    }

    public void cancelPayment(Long id) {
        Payment payment = paymentRepositoryPort.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));
        payment.setStatus(PaymentStatus.CANCELLED);
        paymentRepositoryPort.save(payment);
    }

    private PaymentResponseDto mapToResponse(Payment payment) {
        return PaymentResponseDto.builder()
                .id(payment.getId())
                .userId(payment.getUser().getId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .paymentDate(payment.getPaymentDate())
                .expirationDate(payment.getExpirationDate())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
