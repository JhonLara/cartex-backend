package com.cartex.infrastructure.rest.controller;

import com.cartex.application.dto.PaymentRequestDto;
import com.cartex.application.dto.PaymentResponseDto;
import com.cartex.application.usecase.PaymentUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentUseCase paymentUseCase;

    @PostMapping
    public ResponseEntity<PaymentResponseDto> createPayment(@Valid @RequestBody PaymentRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentUseCase.createPayment(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponseDto> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentUseCase.getPaymentById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentResponseDto>> getPaymentsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(paymentUseCase.getPaymentsByUserId(userId));
    }

    @GetMapping("/user/{userId}/active")
    public ResponseEntity<Boolean> hasActivePayment(@PathVariable Long userId) {
        return ResponseEntity.ok(paymentUseCase.hasActivePayment(userId));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelPayment(@PathVariable Long id) {
        paymentUseCase.cancelPayment(id);
        return ResponseEntity.noContent().build();
    }
}
