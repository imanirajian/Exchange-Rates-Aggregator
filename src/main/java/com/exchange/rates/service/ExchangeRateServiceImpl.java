package com.exchange.rates.service;

import com.exchange.rates.dto.ExchangeRateResponseDTO;
import com.exchange.rates.service.client.ExchangeRateClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @author Iman Irajian
 * Date: 5/25/2025 09:20 PM
 */

@Log4j2
@Service
@RequiredArgsConstructor
public class ExchangeRateServiceImpl implements ExchangeRateService {

    private final List<ExchangeRateClient> exchangeRateClients;
    private final MetricsService metricsService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final Duration CACHE_TTL = Duration.ofMinutes(10); // configurable TTL

    private String getCacheKey(String baseCurrency, String symbols) {
        return String.format("exchangeRates::%s::%s", baseCurrency, symbols);
    }

    @Override
    public Mono<ExchangeRateResponseDTO> getExchangeRates(String baseCurrency, String symbols) {
        String cacheKey = getCacheKey(baseCurrency, symbols);

        // Try to retrieve from cache
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof ExchangeRateResponseDTO cachedResponse) {
            log.info("Cache hit for key: {}", cacheKey);
            for (ExchangeRateClient exchangeRateClient : exchangeRateClients) {
                String client = exchangeRateClient.getDataSourceName();
                metricsService.incrementRequestCount(client);
                metricsService.incrementResponseCount(client);
                metricsService.incrementCacheHit(client);
            }
            return Mono.just(cachedResponse);
        }

        log.info("Cache miss for key: {}", cacheKey);
        return getFreshExchangeRates(baseCurrency, symbols)
                .doOnSuccess(response -> {
                    if (response != null) {
                        redisTemplate.opsForValue().set(cacheKey, response, CACHE_TTL);
                        log.info("Cached response for key: {}", cacheKey);
                    }
                });
    }

    @Override
    public Mono<ExchangeRateResponseDTO> getFreshExchangeRates(String baseCurrency, String symbols) {
        return Flux.fromIterable(exchangeRateClients)
                .flatMap(client -> {
                    metricsService.incrementRequestCount(client.getDataSourceName());
                    return client.getExchangeRates(baseCurrency, symbols)
                            .doOnSuccess(response -> {
                                if (response != null) {
                                    metricsService.incrementResponseCount(client.getDataSourceName());
                                } else {
                                    metricsService.incrementFailedCount(client.getDataSourceName());
                                }
                            })
                            .onErrorResume(e -> Mono.empty());
                })
                .collectList()
                .flatMap(responses -> {
                    if (responses.isEmpty()) {
                        return Mono.error(new RuntimeException("No exchange rate data available from any provider"));
                    }

                    // Calculate average rates
                    Map<String, Double> averageRates = calculateAverageRates(responses);

                    ExchangeRateResponseDTO response = ExchangeRateResponseDTO.builder()
                            .datasource("Aggregated from multiple sources")
                            .base(baseCurrency)
                            .rates(averageRates)
                            .build();

                    return Mono.just(response);
                });
    }

    private Map<String, Double> calculateAverageRates(List<ExchangeRateResponseDTO> responses) {
        Map<String, List<Double>> ratesByCurrency = new ConcurrentHashMap<>();

        responses.stream()
                .filter(r -> r.getRates() != null)
                .flatMap(r -> r.getRates().entrySet().stream())
                .forEach(entry -> {
                    ratesByCurrency.computeIfAbsent(entry.getKey(), k -> new CopyOnWriteArrayList<>())
                            .add(entry.getValue());
                });

        return ratesByCurrency.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .mapToDouble(Double::doubleValue)
                                .average()
                                .orElse(0.0)
                ));
    }
}