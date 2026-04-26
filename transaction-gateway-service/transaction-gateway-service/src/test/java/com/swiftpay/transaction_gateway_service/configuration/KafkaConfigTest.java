package com.swiftpay.transaction_gateway_service.configuration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class KafkaConfigTest {

    private KafkaConfig kafkaConfig;

    @BeforeEach
    void setup() {
        kafkaConfig = new KafkaConfig();

        ReflectionTestUtils.setField(
                kafkaConfig,
                "bootstrapServers",
                "localhost:9092"
        );
    }

    @Test
    void shouldCreateProducerFactory() {
        ProducerFactory<String, String> producerFactory =
                kafkaConfig.producerFactory();

        assertNotNull(producerFactory);
    }

    @Test
    void shouldCreateKafkaTemplate() {
        KafkaTemplate<String, String> kafkaTemplate =
                kafkaConfig.kafkaTemplate();

        assertNotNull(kafkaTemplate);
    }

    @Test
    void shouldCreateConsumerFactory() {
        ConsumerFactory<String, String> consumerFactory =
                kafkaConfig.consumerFactory();

        assertNotNull(consumerFactory);
    }

    @Test
    void shouldCreateKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                kafkaConfig.kafkaListenerContainerFactory();

        assertNotNull(factory);
        assertNotNull(factory.getConsumerFactory());
        assertNotNull(factory.getContainerProperties());
    }
}