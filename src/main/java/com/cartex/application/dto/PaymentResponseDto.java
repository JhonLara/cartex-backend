package com.cartex.application.dto;

import com.cartex.domain.model.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponseDto {

    private Long id;
    private Long userId;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private LocalDateTime paymentDate;
    private LocalDateTime expirationDate;
    private LocalDateTime createdAt;
}
