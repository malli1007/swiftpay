package com.swiftpay.transaction_gateway_service.controller;

// ===============================
// 6. PaymentController.java
// ===============================

import com.swiftpay.transaction_gateway_service.model.PaymentRequest;
import com.swiftpay.transaction_gateway_service.model.PaymentResponse;
import com.swiftpay.transaction_gateway_service.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public PaymentResponse createPayment(
            @Valid @RequestBody PaymentRequest request) {

       return paymentService.processPayment(request);
    }
}
