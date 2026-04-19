package com.swiftpay.transaction_gateway_service.entity;

// ===============================
// 3. PaymentTransaction.java
// ===============================

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String transactionId;

    private Long senderId;
    private Long receiverId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String message;

    private LocalDateTime createdAt;
}
