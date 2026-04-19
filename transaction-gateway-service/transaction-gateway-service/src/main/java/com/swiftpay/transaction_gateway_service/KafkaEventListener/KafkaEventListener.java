package com.swiftpay.transaction_gateway_service.KafkaEventListener;

import com.swiftpay.transaction_gateway_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaEventListener {

    private final PaymentService paymentService;

    @KafkaListener(topics = "payment-completed")
    public void paymentInitiatedListenerCompleted(String event) {
        paymentService.processCompleted(event);
    }

    @KafkaListener(topics = "payment-failed")
    public void paymentInitiatedListenerFailed(String event) {
        paymentService.processFailed(event);
    }
}