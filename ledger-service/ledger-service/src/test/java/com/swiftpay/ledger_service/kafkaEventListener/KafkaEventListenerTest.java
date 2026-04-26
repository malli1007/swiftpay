package com.swiftpay.ledger_service.kafkaEventListener;

import com.swiftpay.ledger_service.service.LedgerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;

class KafkaEventListenerTest {

    private LedgerService ledgerService;
    private KafkaEventListener kafkaEventListener;

    @BeforeEach
    void setup() {
        ledgerService = Mockito.mock(LedgerService.class);
        kafkaEventListener = new KafkaEventListener(ledgerService);
    }

    @Test
    void paymentInitiatedListener_shouldCallProcessTransfer() {

        String event = """
            {
              "transactionId":"TXN1001",
              "senderId":1,
              "receiverId":2,
              "amount":500,
              "currency":"INR"
            }
            """;

        kafkaEventListener.paymentInitiatedListener(event);

        verify(ledgerService, times(1))
                .processTransfer(event);
    }

    @Test
    void paymentInitiatedListener_shouldHandleEmptyMessage() {

        String event = "";

        kafkaEventListener.paymentInitiatedListener(event);

        verify(ledgerService, times(1))
                .processTransfer(event);
    }

    @Test
    void dltListener_shouldOnlyConsumeMessage() {

        String event = """
            {
              "transactionId":"TXN9999",
              "status":"FAILED"
            }
            """;

        kafkaEventListener.dltListener(event);

        verify(ledgerService, never())
                .processTransfer(event);
    }
}