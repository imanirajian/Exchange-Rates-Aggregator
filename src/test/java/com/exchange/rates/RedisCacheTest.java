package com.exchange.rates;

import com.exchange.rates.config.RedisConfig;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Iman Irajian
 * Date: 5/26/2025 1:40 AM
 */

@SpringBootTest
class RedisCacheTest {

    @Autowired
    RedisCacheManager redisCacheManager;

    @Test
    void testExchangeRatesCacheTtl() {
        RedisCacheConfiguration config = redisCacheManager.getCacheConfigurations().get("exchangeRates");

        assertThat(config).isNotNull();
        assertThat(config.getTtl()).isEqualTo(Duration.ofHours(1));
    }

    private final RedisConfig redisConfig = new RedisConfig();

    @Test
    void redisTemplate_shouldBeConfiguredProperly() {
        RedisConnectionFactory mockConnectionFactory = Mockito.mock(RedisConnectionFactory.class);

        RedisTemplate<String, Object> template = redisConfig.redisTemplate(mockConnectionFactory);

        assertNotNull(template);
        assertSame(mockConnectionFactory, template.getConnectionFactory());

        assertInstanceOf(StringRedisSerializer.class, template.getKeySerializer());
        assertInstanceOf(GenericJackson2JsonRedisSerializer.class, template.getValueSerializer());
        assertInstanceOf(StringRedisSerializer.class, template.getHashKeySerializer());
        assertInstanceOf(GenericJackson2JsonRedisSerializer.class, template.getHashValueSerializer());
    }

}
