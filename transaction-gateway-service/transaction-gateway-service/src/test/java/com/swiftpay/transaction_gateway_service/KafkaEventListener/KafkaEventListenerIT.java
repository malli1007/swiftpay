package com.swiftpay.transaction_gateway_service.KafkaEventListener;

import com.swiftpay.transaction_gateway_service.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Testcontainers
class KafkaEventListenerIT {

    @Container
    static KafkaContainer kafka =
            new KafkaContainer(
                    DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.kafka.bootstrap-servers",
                kafka::getBootstrapServers
        );
    }

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;


    @MockitoBean
    private PaymentService paymentService;

    @Test
    void shouldConsumePaymentCompletedTopic() {

        String event = """
            {
              "transactionId":"TXN1001",
              "status":"SUCCESS",
              "message":"Completed"
            }
            """;

        kafkaTemplate.send("payment-completed", event);

        verify(paymentService, timeout(5000))
                .processCompleted(event);
    }

    @Test
    void shouldConsumePaymentFailedTopic() {

        String event = """
            {
              "transactionId":"TXN1002",
              "status":"FAILED",
              "message":"Failed"
            }
            """;

        kafkaTemplate.send("payment-failed", event);

        verify(paymentService, timeout(5000))
                .processFailed(event);
    }
}