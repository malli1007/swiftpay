package com.swiftpay.ledger_service.configuration;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

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

        ProducerFactory<String, String> factory =
                kafkaConfig.producerFactory();

        assertNotNull(factory);
        assertInstanceOf(DefaultKafkaProducerFactory.class, factory);

        DefaultKafkaProducerFactory<?, ?> pf =
                (DefaultKafkaProducerFactory<?, ?>) factory;

        assertEquals(
                "localhost:9092",
                pf.getConfigurationProperties()
                        .get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG)
        );

        assertEquals(
                org.apache.kafka.common.serialization.StringSerializer.class,
                pf.getConfigurationProperties()
                        .get(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG)
        );

        assertEquals(
                org.apache.kafka.common.serialization.StringSerializer.class,
                pf.getConfigurationProperties()
                        .get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG)
        );
    }

    @Test
    void shouldCreateKafkaTemplate() {

        KafkaTemplate<String, String> template =
                kafkaConfig.kafkaTemplate();

        assertNotNull(template);
    }

    @Test
    void shouldCreateConsumerFactory() {

        ConsumerFactory<String, String> factory =
                kafkaConfig.consumerFactory();

        assertNotNull(factory);
        assertInstanceOf(DefaultKafkaConsumerFactory.class, factory);

        DefaultKafkaConsumerFactory<?, ?> cf =
                (DefaultKafkaConsumerFactory<?, ?>) factory;

        assertEquals(
                "localhost:9092",
                cf.getConfigurationProperties()
                        .get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG)
        );

        assertEquals(
                "swiftpay-group-ledger",
                cf.getConfigurationProperties()
                        .get(ConsumerConfig.GROUP_ID_CONFIG)
        );

        assertEquals(
                "earliest",
                cf.getConfigurationProperties()
                        .get(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG)
        );

        assertEquals(
                false,
                cf.getConfigurationProperties()
                        .get(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG)
        );
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