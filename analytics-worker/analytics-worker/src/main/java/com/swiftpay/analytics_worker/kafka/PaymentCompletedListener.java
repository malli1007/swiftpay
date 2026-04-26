package com.swiftpay.analytics_worker.kafka;

import com.swiftpay.analytics_worker.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentCompletedListener {

    private final AnalyticsService analyticsService;

    @KafkaListener(
            topics = "payment-completed",
            groupId = "swiftpay-group-analytics"
    )
    public void paymentCompleted(String message) {
        log.info("Received payment-completed event: {}", message);
        analyticsService.saveAnalytics(message);
    }

    @KafkaListener(
            topics = "payment-failed",
            groupId = "swiftpay-group-analytics"
    )
    public void paymentFailed(String message) {
        log.info("Received payment-failed event: {}", message);
        analyticsService.saveFailedAnalytics(message);
    }

    @KafkaListener(
            topics = {"payment-completed.DLT", "payment-failed.DLT"},
            groupId = "swiftpay-analytics-dlt-group"
    )
    public void dltListener(String message) {
        log.error("Analytics event moved to DLT: {}", message);
    }
}