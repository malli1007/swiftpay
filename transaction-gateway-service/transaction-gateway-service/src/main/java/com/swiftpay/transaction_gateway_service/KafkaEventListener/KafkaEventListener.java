package com.swiftpay.transaction_gateway_service.KafkaEventListener;

import com.swiftpay.transaction_gateway_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaEventListener {

    private final PaymentService paymentService;

    @KafkaListener(
            topics = "payment-completed",
            groupId = "swiftpay-group-transaction"
    )
    public void paymentCompleted(String event) {
        log.info("Received payment-completed event: {}", event);
        paymentService.processCompleted(event);
    }

    @KafkaListener(
            topics = "payment-failed",
            groupId = "swiftpay-group-transaction"
    )
    public void paymentFailed(String event) {
        log.info("Received payment-failed event: {}", event);
        paymentService.processFailed(event);
    }

    @KafkaListener(
            topics = {"payment-completed.DLT", "payment-failed.DLT"},
            groupId = "swiftpay-dlt-group"
    )
    public void dltListener(String event) {
        log.error("Moved to Dead Letter Topic: {}", event);
    }
}