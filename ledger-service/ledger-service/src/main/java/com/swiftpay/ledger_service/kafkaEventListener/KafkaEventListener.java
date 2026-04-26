package com.swiftpay.ledger_service.kafkaEventListener;

import com.swiftpay.ledger_service.service.LedgerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaEventListener {

    private final LedgerService ledgerService;

    @KafkaListener(
            topics = "payment-initiated",
            groupId = "swiftpay-group-ledger"
    )
    public void paymentInitiatedListener(String event) {
        log.info("Received payment-initiated event: {}", event);
        ledgerService.processTransfer(event);
    }

    @KafkaListener(
            topics = "payment-initiated.DLT",
            groupId = "swiftpay-ledger-dlt-group"
    )
    public void dltListener(String event) {
        log.error("Moved to DLT: {}", event);
    }
}