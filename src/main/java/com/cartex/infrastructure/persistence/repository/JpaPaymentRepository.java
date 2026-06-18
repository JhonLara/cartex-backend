package com.cartex.infrastructure.persistence.repository;

import com.cartex.domain.model.PaymentStatus;
import com.cartex.infrastructure.persistence.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaPaymentRepository extends JpaRepository<PaymentEntity, Long> {

    List<PaymentEntity> findByUserId(Long userId);

    List<PaymentEntity> findByUserIdAndStatus(Long userId, PaymentStatus status);
}
