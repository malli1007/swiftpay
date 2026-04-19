package com.swiftpay.transaction_gateway_service.model;

// ===============================
// 1. PaymentRequest.java
// ===============================

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRequest {

    @NotBlank
    private String transactionId;

    @NotNull
    private Long senderId;

    @NotNull
    private Long receiverId;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotBlank
    private String currency;
}