package com.exchange.rates.service;

import com.exchange.rates.dto.ExchangeRateResponseDTO;
import com.exchange.rates.service.client.ExchangeRateClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

/**
 * @author Iman Irajian
 * Date: 5/25/2025 09:20 PM
 */

@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceImplTest {

    @Mock
    ExchangeRateClient client1;

    @Mock
    ExchangeRateClient client2;

    @Mock
    MetricsService metricsService;

    @Mock
    RedisTemplate<String, Object> redisTemplate;

    @Mock
    ValueOperations<String, Object> valueOps;

    @InjectMocks
    ExchangeRateServiceImpl service;

    private static final String BASE = "USD";
    private static final String SYMBOLS = "EUR,GBP";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ExchangeRateServiceImpl(List.of(client1, client2), metricsService, redisTemplate);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    @Test
    void whenCacheHit_thenReturnCachedResponse() {
        ExchangeRateResponseDTO cachedResponse = ExchangeRateResponseDTO.builder()
                .base(BASE)
                .rates(Map.of("EUR", 1.1, "GBP", 0.9))
                .datasource("cached")
                .build();

        String cacheKey = "exchangeRates::USD::EUR,GBP";
        when(valueOps.get(cacheKey)).thenReturn(cachedResponse);
        when(client1.getDataSourceName()).thenReturn("client1");
        when(client2.getDataSourceName()).thenReturn("client2");

        StepVerifier.create(service.getExchangeRates(BASE, SYMBOLS))
                .expectNextMatches(r -> r.getDatasource().equals("cached"))
                .verifyComplete();

        verify(metricsService, times(1)).incrementCacheHit("client1");
        verify(metricsService, times(1)).incrementCacheHit("client2");
    }

    @Test
    void whenCacheMiss_thenAggregateRatesAndStoreInCache() {
        String cacheKey = "exchangeRates::USD::EUR,GBP";
        when(valueOps.get(cacheKey)).thenReturn(null);

        ExchangeRateResponseDTO r1 = ExchangeRateResponseDTO.builder()
                .base(BASE)
                .rates(Map.of("EUR", 1.1, "GBP", 0.9))
                .build();

        ExchangeRateResponseDTO r2 = ExchangeRateResponseDTO.builder()
                .base(BASE)
                .rates(Map.of("EUR", 1.3, "GBP", 1.1))
                .build();

        when(client1.getDataSourceName()).thenReturn("client1");
        when(client2.getDataSourceName()).thenReturn("client2");
        when(client1.getExchangeRates(BASE, SYMBOLS)).thenReturn(Mono.just(r1));
        when(client2.getExchangeRates(BASE, SYMBOLS)).thenReturn(Mono.just(r2));

        StepVerifier.create(service.getExchangeRates(BASE, SYMBOLS))
                .expectNextMatches(result -> {
                    double eur = result.getRates().get("EUR");
                    double gbp = result.getRates().get("GBP");
                    return result.getDatasource().contains("Aggregated")
                            && Math.abs(eur - 1.2) < 0.0001
                            && Math.abs(gbp - 1.0) < 0.0001;
                })
                .verifyComplete();

        verify(valueOps).set(eq(cacheKey), any(), any());
    }

    @Test
    void whenNoResponses_thenReturnError() {
        when(valueOps.get(any())).thenReturn(null);

        when(client1.getDataSourceName()).thenReturn("client1");
        when(client2.getDataSourceName()).thenReturn("client2");

        when(client1.getExchangeRates(BASE, SYMBOLS)).thenReturn(Mono.empty());
        when(client2.getExchangeRates(BASE, SYMBOLS)).thenReturn(Mono.empty());

        StepVerifier.create(service.getExchangeRates(BASE, SYMBOLS))
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().contains("No exchange rate data")
                )
                .verify();
    }

    @Test
    void whenSomeClientsFail_thenStillReturnValidResponse() {
        when(valueOps.get(any())).thenReturn(null);

        ExchangeRateResponseDTO validResponse = ExchangeRateResponseDTO.builder()
                .base(BASE)
                .rates(Map.of("EUR", 1.2, "GBP", 1.0))
                .build();

        when(client1.getDataSourceName()).thenReturn("client1");
        when(client2.getDataSourceName()).thenReturn("client2");

        when(client1.getExchangeRates(BASE, SYMBOLS)).thenReturn(Mono.error(new RuntimeException("fail")));
        when(client2.getExchangeRates(BASE, SYMBOLS)).thenReturn(Mono.just(validResponse));

        StepVerifier.create(service.getExchangeRates(BASE, SYMBOLS))
                .expectNextMatches(r -> r.getRates().get("EUR") == 1.2)
                .verifyComplete();
    }
}