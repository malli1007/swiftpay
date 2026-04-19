package com.swiftpay.transaction_gateway_service.service;


import com.swiftpay.transaction_gateway_service.entity.PaymentTransaction;
import com.swiftpay.transaction_gateway_service.model.PaymentInitiatedEvent;
import com.swiftpay.transaction_gateway_service.model.PaymentRequest;
import com.swiftpay.transaction_gateway_service.model.PaymentResponse;
import com.swiftpay.transaction_gateway_service.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final StringRedisTemplate redisTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper=new ObjectMapper();
    // Call Ledger Service
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String TOPIC = "payment-initiated";

    public PaymentResponse processPayment(PaymentRequest request) {

        String transactionId = request.getTransactionId();

        // ==========================================
        // 1. Idempotency Check
        // ==========================================
        if (Boolean.TRUE.equals(redisTemplate.hasKey(transactionId))) {
            return new PaymentResponse(
                    transactionId,
                    "FAILED",
                    "Duplicate transaction request"
            );
        }

        // Lock transactionId for 24 hours
        redisTemplate.opsForValue()
                .set(transactionId, "PROCESSED", Duration.ofHours(24));

        // ==========================================
        // 2. Validate Sender Balance
        // ==========================================
        BigDecimal senderBalance = getSenderBalance(request.getSenderId());

        if (senderBalance.compareTo(request.getAmount()) < 0) {
            return new PaymentResponse(
                    transactionId,
                    "FAILED",
                    "Insufficient balance"
            );
        }

        // ==========================================
        // 3. Save Payment Request in PostgreSQL
        // ==========================================
        PaymentTransaction payment = PaymentTransaction.builder()
                .transactionId(transactionId)
                .senderId(request.getSenderId())
                .receiverId(request.getReceiverId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        paymentRepository.save(payment);

        // ==========================================
        // 4. Publish Event to Kafka
        // ==========================================
        PaymentInitiatedEvent event = new PaymentInitiatedEvent(
                transactionId,
                request.getSenderId(),
                request.getReceiverId(),
                request.getAmount(),
                request.getCurrency()
        );

        kafkaTemplate.send(TOPIC, new ObjectMapper().writeValueAsString(event));

        // ==========================================
        // 5. Response
        // ==========================================
        return new PaymentResponse(
                transactionId,
                "SUCCESS",
                "Payment initiated successfully"
        );
    }

    // ==========================================
    // Call Ledger Service API
    // Example:
    // GET http://localhost:8082/v1/accounts/{userId}/balance
    // ==========================================
    private BigDecimal getSenderBalance(Long senderId) {

        String url =
                "http://localhost:8082/v1/transactions/accounts/"
                        + senderId +
                        "/balance";

        try {
            return restTemplate.getForObject(url, BigDecimal.class);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Unable to fetch balance from Ledger Service"
            );
        }
    }


    public void processCompleted(String message) {
        PaymentResponse paymentResponse = objectMapper.readValue(message, PaymentResponse.class);

        PaymentTransaction paymentTransaction = paymentRepository.findByTransactionId(paymentResponse.getTransactionId());

        paymentTransaction.setStatus(paymentResponse.getStatus());
        paymentTransaction.setMessage(paymentResponse.getMessage());
        paymentRepository.save(paymentTransaction);
    }

    public void processFailed(String message) {
        PaymentResponse paymentResponse = objectMapper.readValue(message, PaymentResponse.class);

        PaymentTransaction paymentTransaction = paymentRepository.findByTransactionId(paymentResponse.getTransactionId());

        paymentTransaction.setMessage(paymentResponse.getMessage());
        paymentTransaction.setStatus("FAILED");
        paymentRepository.save(paymentTransaction);
    }
}