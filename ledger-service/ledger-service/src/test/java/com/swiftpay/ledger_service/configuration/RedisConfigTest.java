package com.swiftpay.ledger_service.configuration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class RedisConfigTest {

    private RedisConfig redisConfig;

    @BeforeEach
    void setup() {
        redisConfig = new RedisConfig();

        ReflectionTestUtils.setField(
                redisConfig,
                "redisHost",
                "localhost"
        );

        ReflectionTestUtils.setField(
                redisConfig,
                "redisPort",
                6379
        );
    }

    @Test
    void shouldCreateRedisConnectionFactory() {
        RedisConnectionFactory connectionFactory =
                redisConfig.redisConnectionFactory();

        assertNotNull(connectionFactory);
    }

    @Test
    void shouldCreateRedisTemplate() {
        RedisConnectionFactory connectionFactory =
                redisConfig.redisConnectionFactory();

        RedisTemplate<String, String> redisTemplate =
                redisConfig.redisTemplate(connectionFactory);

        assertNotNull(redisTemplate);
        assertNotNull(redisTemplate.getConnectionFactory());
        assertNotNull(redisTemplate.getKeySerializer());
        assertNotNull(redisTemplate.getValueSerializer());
        assertNotNull(redisTemplate.getHashKeySerializer());
        assertNotNull(redisTemplate.getHashValueSerializer());
    }
}