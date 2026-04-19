package com.swiftpay.analytics_worker.service;

import com.swiftpay.analytics_worker.entity.AnalyticsRecord;
import com.swiftpay.analytics_worker.model.PaymentCompletedEvent;
import com.swiftpay.analytics_worker.model.PaymentFailedEvent;
import com.swiftpay.analytics_worker.repository.AnalyticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final AnalyticsRepository repository;

    private final ObjectMapper objectMapper=new ObjectMapper();

    public void saveAnalytics(String message) {

        PaymentCompletedEvent event =new ObjectMapper().readValue(message,PaymentCompletedEvent.class);

        AnalyticsRecord record = AnalyticsRecord.builder()
                .transactionId(event.getTransactionId())
                .status(event.getStatus())
                .processedAt(LocalDateTime.now())
                .build();

        repository.save(record);
    }

    public void saveFailedAnalytics(String message) {

        PaymentFailedEvent event =new ObjectMapper().readValue(message, PaymentFailedEvent.class);

        AnalyticsRecord record = AnalyticsRecord.builder()
                .transactionId(event.getTransactionId())
                .status(event.getMessage())
                .processedAt(LocalDateTime.now())
                .build();

        repository.save(record);
    }


}
