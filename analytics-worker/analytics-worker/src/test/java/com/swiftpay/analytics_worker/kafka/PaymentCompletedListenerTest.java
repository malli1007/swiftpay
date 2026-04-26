package com.swiftpay.analytics_worker.kafka;

import com.swiftpay.analytics_worker.service.AnalyticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class PaymentCompletedListenerTest {

    private AnalyticsService analyticsService;
    private PaymentCompletedListener listener;

    @BeforeEach
    void setup() {
        analyticsService = Mockito.mock(AnalyticsService.class);
        listener = new PaymentCompletedListener(analyticsService);
    }

    @Test
    void paymentCompleted_shouldCallSaveAnalytics() {

        String message = """
            {
              "transactionId":"TXN1001",
              "status":"SUCCESS",
              "message":"Transferred successfully"
            }
            """;

        listener.paymentCompleted(message);

        verify(analyticsService, times(1))
                .saveAnalytics(message);
    }

    @Test
    void paymentFailed_shouldCallSaveFailedAnalytics() {

        String message = """
            {
              "transactionId":"TXN1002",
              "status":"FAILED",
              "message":"Receiver not found"
            }
            """;

        listener.paymentFailed(message);

        verify(analyticsService, times(1))
                .saveFailedAnalytics(message);
    }

    @Test
    void paymentCompleted_shouldHandleEmptyMessage() {

        String message = "";

        listener.paymentCompleted(message);

        verify(analyticsService, times(1))
                .saveAnalytics(message);
    }

    @Test
    void paymentFailed_shouldHandleEmptyMessage() {

        String message = "";

        listener.paymentFailed(message);

        verify(analyticsService, times(1))
                .saveFailedAnalytics(message);
    }

    @Test
    void dltListener_shouldNotCallService() {

        String message = """
            {
              "transactionId":"TXN9999"
            }
            """;

        listener.dltListener(message);

        verify(analyticsService, never())
                .saveAnalytics(message);

        verify(analyticsService, never())
                .saveFailedAnalytics(message);
    }
}