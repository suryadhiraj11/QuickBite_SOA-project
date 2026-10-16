package com.quickbite.payment.controller;

import com.quickbite.payment.dto.CreatePaymentRequest;
import com.quickbite.payment.entity.Payment;
import com.quickbite.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(
            PaymentService paymentService) {

        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<Payment> createPayment(
            @Valid @RequestBody CreatePaymentRequest request,
            Authentication authentication) {

        String customerEmail =
                authentication.getName();

        Payment payment =
                paymentService.createPayment(
                        request,
                        customerEmail
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(payment);
    }
    
    @PostMapping("/{id}/process")
    public ResponseEntity<Payment> processPayment(
            @PathVariable Long id,
            Authentication authentication) {

        Payment payment =
                paymentService.processPayment(
                        id,
                        authentication.getName()
                );

        return ResponseEntity.ok(payment);
    }
    
}