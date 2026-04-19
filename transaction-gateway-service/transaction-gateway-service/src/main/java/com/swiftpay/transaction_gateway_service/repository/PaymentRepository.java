package com.swiftpay.transaction_gateway_service.repository;

import com.swiftpay.transaction_gateway_service.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentTransaction, Long> {
    PaymentTransaction findByTransactionId(String transactionId);
}
