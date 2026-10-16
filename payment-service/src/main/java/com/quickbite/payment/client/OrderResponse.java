package com.quickbite.payment.client;

import java.math.BigDecimal;

public class OrderResponse {

    private Long id;
    private String customerEmail;
    private BigDecimal totalAmount;
    private String status;

    public Long getId() {
        return id;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getStatus() {
        return status;
    }
}