package com.quickbite.payment.service;

import com.quickbite.payment.client.OrderClient;
import com.quickbite.payment.client.OrderResponse;
import com.quickbite.payment.dto.CreatePaymentRequest;
import com.quickbite.payment.entity.Payment;
import com.quickbite.payment.entity.PaymentStatus;
import com.quickbite.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderClient orderClient;

    public PaymentService(
            PaymentRepository paymentRepository,
            OrderClient orderClient) {

        this.paymentRepository = paymentRepository;
        this.orderClient = orderClient;
    }

    @Transactional
    public Payment createPayment(
            CreatePaymentRequest request,
            String customerEmail) {

        Payment existingPayment =
                paymentRepository
                        .findByOrderId(request.getOrderId())
                        .orElse(null);

        if (existingPayment != null) {
            throw new IllegalStateException(
                    "Payment already exists for this order"
            );
        }

        OrderResponse order =
                orderClient.getOrder(request.getOrderId());

        if (order == null) {
            throw new RuntimeException("Order not found");
        }

        if (!order.getCustomerEmail()
                .equals(customerEmail)) {

            throw new org.springframework.security.access
                    .AccessDeniedException(
                            "You are not allowed to pay for this order"
                    );
        }

        Payment payment = new Payment();

        payment.setOrderId(order.getId());
        payment.setCustomerEmail(customerEmail);
        payment.setAmount(order.getTotalAmount());
        payment.setPaymentMethod(
                request.getPaymentMethod()
        );

        payment.setStatus(PaymentStatus.PENDING);

        return paymentRepository.save(payment);
    }
    
    @Transactional
    public Payment processPayment(
            Long paymentId,
            String customerEmail) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new RuntimeException("Payment not found"));

        if (!payment.getCustomerEmail().equals(customerEmail)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You are not allowed to process this payment"
            );
        }

        // Idempotent behavior:
        // If already completed, return the existing result.
        if (payment.getStatus() == PaymentStatus.SUCCESS ||
                payment.getStatus() == PaymentStatus.FAILED) {

            return payment;
        }

        // Only PENDING reaches the payment-processing logic.
        boolean paymentSuccessful = true;

        if (paymentSuccessful) {

            payment.setStatus(PaymentStatus.SUCCESS);

            payment.setTransactionId(
                    "TXN-" + UUID.randomUUID()
                            .toString()
                            .substring(0, 8)
                            .toUpperCase()
            );

            paymentRepository.save(payment);

            orderClient.updatePaymentStatus(
                    payment.getOrderId(),
                    "ACCEPTED"
            );

        } else {

            payment.setStatus(PaymentStatus.FAILED);

            payment.setTransactionId(
                    "FAILED-" + UUID.randomUUID()
                            .toString()
                            .substring(0, 8)
                            .toUpperCase()
            );

            paymentRepository.save(payment);

            orderClient.updatePaymentStatus(
                    payment.getOrderId(),
                    "PAYMENT_FAILED"
            );
        }

        return payment;
    }
    
}