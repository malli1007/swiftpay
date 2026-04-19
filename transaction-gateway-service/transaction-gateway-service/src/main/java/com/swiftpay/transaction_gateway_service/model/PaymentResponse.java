package com.swiftpay.transaction_gateway_service.model;

// ===============================
// 2. PaymentResponse.java
// ===============================

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentResponse {

    private String transactionId;
    private String status;
    private String message;
}
