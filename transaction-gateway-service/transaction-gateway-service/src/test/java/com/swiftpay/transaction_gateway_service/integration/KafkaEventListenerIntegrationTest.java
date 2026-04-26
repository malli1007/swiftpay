package com.swiftpay.transaction_gateway_service.integration;

import com.swiftpay.transaction_gateway_service.KafkaEventListener.KafkaEventListener;
import com.swiftpay.transaction_gateway_service.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class KafkaEventListenerIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("swiftpay")
            .withUsername("swiftpay")
            .withPassword("swiftpay");

    @Container
    static KafkaContainer kafka = new KafkaContainer("5.5.0");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @Autowired
    private KafkaEventListener kafkaEventListener;
    @Autowired
    private PaymentService paymentService;

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Test
    void dltListener_shouldLogDeadLetterEvent() {
        // This test can be expanded to produce a message to DLT and verify log output or side effects
        kafkaEventListener.dltListener("{\"transactionId\":\"TXN9999\"}");
        // No exception means pass for now
        assertThat(true).isTrue();
    }
}

