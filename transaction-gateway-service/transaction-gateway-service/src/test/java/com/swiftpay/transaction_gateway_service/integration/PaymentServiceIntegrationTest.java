package com.swiftpay.transaction_gateway_service.integration;

import com.swiftpay.transaction_gateway_service.entity.PaymentTransaction;
import com.swiftpay.transaction_gateway_service.model.PaymentRequest;
import com.swiftpay.transaction_gateway_service.model.PaymentResponse;
import com.swiftpay.transaction_gateway_service.repository.PaymentRepository;
import com.swiftpay.transaction_gateway_service.service.PaymentService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;

@SpringBootTest
@Testcontainers
class PaymentServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("swiftpay")
                    .withUsername("swiftpay")
                    .withPassword("swiftpay");

    @Container
    static KafkaContainer kafka =
            new KafkaContainer(
                    DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

    @Container
    static GenericContainer<?> redis =
            new GenericContainer<>(
                    DockerImageName.parse("redis:7-alpine"))
                    .withExposedPorts(6379);

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.kafka.bootstrap-servers",
                kafka::getBootstrapServers);

        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port",
                () -> redis.getMappedPort(6379));

        registry.add("ledger.service.base-url",
                () -> "http://dummy-service");
    }

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();

        PaymentService spy = Mockito.spy(paymentService);

        doReturn(new BigDecimal("1000"))
                .when(spy)
                .getSenderBalance(anyLong());

        paymentService = spy;
    }

    @AfterEach
    void tearDown() {
        paymentRepository.deleteAll();
    }

    @Test
    void processPayment_shouldPersistTransaction() {

        PaymentRequest request = new PaymentRequest();
        request.setTransactionId("TXN2001");
        request.setSenderId(1L);
        request.setReceiverId(2L);
        request.setAmount(new BigDecimal("100.00"));
        request.setCurrency("INR");

        PaymentResponse response =
                paymentService.processPayment(request);

        assertThat(response.getStatus()).isEqualTo("SUCCESS");

        PaymentTransaction txn =
                paymentRepository.findByTransactionId("TXN2001");

        assertThat(txn).isNotNull();
        assertThat(txn.getTransactionId()).isEqualTo("TXN2001");
        assertThat(txn.getAmount())
                .isEqualByComparingTo("100.00");
        assertThat(txn.getStatus()).isEqualTo("PENDING");
    }
}