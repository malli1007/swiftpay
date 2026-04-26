package com.swiftpay.transaction_gateway_service.service;

import com.swiftpay.transaction_gateway_service.entity.PaymentTransaction;
import com.swiftpay.transaction_gateway_service.model.PaymentRequest;
import com.swiftpay.transaction_gateway_service.model.PaymentResponse;
import com.swiftpay.transaction_gateway_service.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private PaymentService paymentService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(
                paymentService,
                "ledgerBaseUrl",
                "http://localhost:8082"
        );
    }

    @Test
    void shouldFailForDuplicateTransaction() {

        when(redisTemplate.hasKey("TXN1001")).thenReturn(true);

        PaymentResponse response =
                paymentService.processPayment(createRequest());

        assertEquals("FAILED", response.getStatus());
        assertEquals("Duplicate transaction request", response.getMessage());
    }

    @Test
    void shouldProcessCompletedEvent() {

        PaymentTransaction payment = new PaymentTransaction();
        payment.setTransactionId("TXN1001");

        when(paymentRepository.findByTransactionId("TXN1001"))
                .thenReturn(payment);

        String message = """
            {
              "transactionId":"TXN1001",
              "status":"SUCCESS",
              "message":"Payment completed"
            }
            """;

        paymentService.processCompleted(message);

        assertEquals("SUCCESS", payment.getStatus());
        assertEquals("Payment completed", payment.getMessage());

        verify(paymentRepository).save(payment);
    }

    @Test
    void shouldProcessFailedEvent() {

        PaymentTransaction payment = new PaymentTransaction();
        payment.setTransactionId("TXN1001");

        when(paymentRepository.findByTransactionId("TXN1001"))
                .thenReturn(payment);

        String message = """
            {
              "transactionId":"TXN1001",
              "status":"FAILED",
              "message":"Insufficient balance"
            }
            """;

        paymentService.processFailed(message);

        assertEquals("FAILED", payment.getStatus());
        assertEquals("Insufficient balance", payment.getMessage());

        verify(paymentRepository).save(payment);
    }

    private PaymentRequest createRequest() {
        PaymentRequest request = new PaymentRequest();
        request.setTransactionId("TXN1001");
        request.setSenderId(1L);
        request.setReceiverId(2L);
        request.setAmount(new BigDecimal("500"));
        request.setCurrency("INR");
        return request;
    }

}