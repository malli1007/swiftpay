package com.swiftpay.ledger_service.KafkaEventListener;

import com.swiftpay.ledger_service.service.LedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaEventListener {

    private final LedgerService ledgerService;

    @KafkaListener(topics = "payment-initiated")
    public void paymentInitiatedListener(String event) {
        ledgerService.processTransfer(event);
    }
}