package com.swiftpay.transaction_gateway_service.controller;

import com.swiftpay.transaction_gateway_service.model.PaymentRequest;
import com.swiftpay.transaction_gateway_service.model.PaymentResponse;
import com.swiftpay.transaction_gateway_service.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment APIs")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(
            summary = "Create Payment",
            description = "Creates a new payment transaction"
    )
    @ApiResponse(responseCode = "200", description = "Payment processed successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    public PaymentResponse createPayment(
            @Valid @RequestBody PaymentRequest request) {

        return paymentService.processPayment(request);
    }
}