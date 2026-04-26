package com.swiftpay.analytics_worker.service;

import com.swiftpay.analytics_worker.entity.AnalyticsRecord;
import com.swiftpay.analytics_worker.repository.AnalyticsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.any;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private AnalyticsRepository repository;

    @InjectMocks
    private AnalyticsService analyticsService;

    @Test
    void saveAnalytics_shouldSaveSuccessRecord() {

        String message = """
            {
              "transactionId":"TXN1001",
              "status":"SUCCESS",
              "message":"Transferred successfully"
            }
            """;

        analyticsService.saveAnalytics(message);

        ArgumentCaptor<AnalyticsRecord> captor =
                ArgumentCaptor.forClass(AnalyticsRecord.class);

        verify(repository, times(1))
                .save(captor.capture());

        AnalyticsRecord saved = captor.getValue();

        assertEquals("TXN1001", saved.getTransactionId());
        assertEquals("SUCCESS", saved.getStatus());
        assertNotNull(saved.getProcessedAt());
    }

    @Test
    void saveFailedAnalytics_shouldSaveFailedRecord() {

        String message = """
            {
              "transactionId":"TXN1002",
              "message":"RECEIVER NOT FOUND"
            }
            """;

        analyticsService.saveFailedAnalytics(message);

        ArgumentCaptor<AnalyticsRecord> captor =
                ArgumentCaptor.forClass(AnalyticsRecord.class);

        verify(repository, times(1))
                .save(captor.capture());

        AnalyticsRecord saved = captor.getValue();

        assertEquals("TXN1002", saved.getTransactionId());
        assertEquals("RECEIVER NOT FOUND", saved.getStatus());
        assertNotNull(saved.getProcessedAt());
    }

    @Test
    void saveAnalytics_shouldThrowForInvalidJson() {

        String message = "invalid-json";

        assertThrows(Exception.class,
                () -> analyticsService.saveAnalytics(message));
    }

    @Test
    void saveFailedAnalytics_shouldThrowForInvalidJson() {

        String message = "invalid-json";

        assertThrows(Exception.class,
                () -> analyticsService.saveFailedAnalytics(message));
    }

    @Test
    void saveAnalytics_shouldHandleMinimalJson() {

        String message = """
            {
              "transactionId":"TXN9999",
              "status":"SUCCESS"
            }
            """;

        analyticsService.saveAnalytics(message);

        verify(repository, times(1))
                .save(any(AnalyticsRecord.class));
    }
}