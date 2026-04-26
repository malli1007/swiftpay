package com.swiftpay.transaction_gateway_service.KafkaEventListener;

import com.swiftpay.transaction_gateway_service.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KafkaEventListenerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private KafkaEventListener kafkaEventListener;

    @Test
    void paymentCompleted_shouldCallProcessCompleted() {
        String event = "{\"transactionId\":\"TXN1001\"}";
        kafkaEventListener.paymentCompleted(event);
        verify(paymentService).processCompleted(event);
    }

    @Test
    void paymentFailed_shouldCallProcessFailed() {
        String event = "{\"transactionId\":\"TXN1002\"}";
        kafkaEventListener.paymentFailed(event);
        verify(paymentService).processFailed(event);
    }
}

