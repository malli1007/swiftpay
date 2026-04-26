package com.swiftpay.transaction_gateway_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swiftpay.transaction_gateway_service.model.PaymentRequest;
import com.swiftpay.transaction_gateway_service.model.PaymentResponse;
import com.swiftpay.transaction_gateway_service.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PaymentControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private PaymentService paymentService;

    @BeforeEach
    void setup() {
        paymentService = Mockito.mock(PaymentService.class);

        PaymentController controller =
                new PaymentController(paymentService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    @Test
    void shouldCreatePaymentSuccessfully() throws Exception {

        when(paymentService.processPayment(any(PaymentRequest.class)))
                .thenReturn(new PaymentResponse(
                        "TXN1001",
                        "SUCCESS",
                        "Payment initiated successfully"
                ));

        mockMvc.perform(post("/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("TXN1001"))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message")
                        .value("Payment initiated successfully"));
    }

    @Test
    void shouldReturnBadRequestWhenTransactionIdMissing() throws Exception {

        PaymentRequest request = validRequest();
        request.setTransactionId(null);

        mockMvc.perform(post("/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenAmountIsZero() throws Exception {

        PaymentRequest request = validRequest();
        request.setAmount(BigDecimal.ZERO);

        mockMvc.perform(post("/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenCurrencyMissing() throws Exception {

        PaymentRequest request = validRequest();
        request.setCurrency(null);

        mockMvc.perform(post("/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    private PaymentRequest validRequest() {
        PaymentRequest request = new PaymentRequest();
        request.setTransactionId("TXN1001");
        request.setSenderId(1L);
        request.setReceiverId(2L);
        request.setAmount(new BigDecimal("500"));
        request.setCurrency("INR");
        return request;
    }
}