package com.swiftpay.analytics_worker.kafka;

import com.swiftpay.analytics_worker.model.PaymentCompletedEvent;
import com.swiftpay.analytics_worker.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentCompletedListener {

    private final AnalyticsService analyticsService;

    @KafkaListener(topics = "payment-completed")
    public void paymentInitiatedListenerCompleted(String message) {
        analyticsService.saveAnalytics(message);
    }

    @KafkaListener(topics = "payment-failed")
    public void paymentInitiatedListenerFailed(String event) {
        analyticsService.saveFailedAnalytics(event);
    }
}
