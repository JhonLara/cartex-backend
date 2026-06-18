package com.cartex.infrastructure.persistence.adapter;

import com.cartex.domain.model.Payment;
import com.cartex.domain.model.PaymentStatus;
import com.cartex.domain.port.PaymentRepositoryPort;
import com.cartex.infrastructure.persistence.entity.PaymentEntity;
import com.cartex.infrastructure.persistence.entity.UserEntity;
import com.cartex.infrastructure.persistence.repository.JpaPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PaymentRepositoryAdapter implements PaymentRepositoryPort {

    private final JpaPaymentRepository jpaPaymentRepository;

    @Override
    public Payment save(Payment payment) {
        PaymentEntity entity = mapToEntity(payment);
        PaymentEntity saved = jpaPaymentRepository.save(entity);
        return mapToDomain(saved);
    }

    @Override
    public Optional<Payment> findById(Long id) {
        return jpaPaymentRepository.findById(id).map(this::mapToDomain);
    }

    @Override
    public List<Payment> findByUserId(Long userId) {
        return jpaPaymentRepository.findByUserId(userId).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Payment> findByUserIdAndStatus(Long userId, PaymentStatus status) {
        return jpaPaymentRepository.findByUserIdAndStatus(userId, status).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        jpaPaymentRepository.deleteById(id);
    }

    private PaymentEntity mapToEntity(Payment payment) {
        UserEntity userEntity = UserEntity.builder()
                .id(payment.getUser().getId())
                .email(payment.getUser().getEmail())
                .password(payment.getUser().getPassword())
                .fullName(payment.getUser().getFullName())
                .role(payment.getUser().getRole())
                .active(payment.getUser().getActive())
                .createdAt(payment.getUser().getCreatedAt())
                .updatedAt(payment.getUser().getUpdatedAt())
                .build();

        return PaymentEntity.builder()
                .id(payment.getId())
                .user(userEntity)
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .paymentDate(payment.getPaymentDate())
                .expirationDate(payment.getExpirationDate())
                .createdAt(payment.getCreatedAt())
                .build();
    }

    private Payment mapToDomain(PaymentEntity entity) {
        return Payment.builder()
                .id(entity.getId())
                .user(com.cartex.domain.model.User.builder()
                        .id(entity.getUser().getId())
                        .email(entity.getUser().getEmail())
                        .password(entity.getUser().getPassword())
                        .fullName(entity.getUser().getFullName())
                        .role(entity.getUser().getRole())
                        .active(entity.getUser().getActive())
                        .createdAt(entity.getUser().getCreatedAt())
                        .updatedAt(entity.getUser().getUpdatedAt())
                        .build())
                .amount(entity.getAmount())
                .currency(entity.getCurrency())
                .status(entity.getStatus())
                .paymentDate(entity.getPaymentDate())
                .expirationDate(entity.getExpirationDate())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
