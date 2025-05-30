package com.exchange.rates.service;

import com.exchange.rates.dto.ApiMetricsDTO;
import com.exchange.rates.dto.ApiMetricsDataDTO;
import com.exchange.rates.dto.MetricsResponseDTO;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * @author Iman Irajian
 * Date: 5/25/2025 09:20 PM
 */

@Service
@RequiredArgsConstructor
public class MetricsServiceImpl implements MetricsService {

    private final MeterRegistry meterRegistry;
    private final ConcurrentHashMap<String, ApiMetricsDataDTO> metricsMap = new ConcurrentHashMap<>();
    private final AtomicLong totalQueries = new AtomicLong(0);

    @Override
    public void incrementRequestCount(String apiName) {
        totalQueries.incrementAndGet();
        metricsMap.compute(apiName, (k, v) -> {
            if (v == null) {
                return ApiMetricsDataDTO.builder().totalRequests(1).build();
            }
            return ApiMetricsDataDTO.builder()
                    .totalRequests(v.getTotalRequests() + 1)
                    .totalResponses(v.getTotalResponses())
                    .failedRequests(v.getFailedRequests())
                    .cacheHits(v.getCacheHits())
                    .build();
        });
        Counter.builder("exchange.rate.requests")
                .tag("api", apiName)
                .register(meterRegistry)
                .increment();
    }

    @Override
    public void incrementResponseCount(String apiName) {
        metricsMap.compute(apiName, (k, v) -> {
            if (v == null) {
                return ApiMetricsDataDTO.builder().totalResponses(1).build();
            }
            return ApiMetricsDataDTO.builder()
                    .totalRequests(v.getTotalRequests())
                    .totalResponses(v.getTotalResponses() + 1)
                    .failedRequests(v.getFailedRequests())
                    .cacheHits(v.getCacheHits())
                    .build();
        });
        Counter.builder("exchange.rate.responses")
                .tag("api", apiName)
                .register(meterRegistry)
                .increment();
    }

    @Override
    public void incrementFailedCount(String apiName) {
        metricsMap.compute(apiName, (k, v) -> {
            if (v == null) {
                return ApiMetricsDataDTO.builder().failedRequests(1).build();
            }
            return ApiMetricsDataDTO.builder()
                    .totalRequests(v.getTotalRequests())
                    .totalResponses(v.getTotalResponses())
                    .failedRequests(v.getFailedRequests() + 1)
                    .cacheHits(v.getCacheHits())
                    .build();
        });
        Counter.builder("exchange.rate.failures")
                .tag("api", apiName)
                .register(meterRegistry)
                .increment();
    }

    @Override
    public void incrementCacheHit(String apiName) {
        metricsMap.compute(apiName, (k, v) -> {
            if (v == null) {
                return ApiMetricsDataDTO.builder().cacheHits(1).build();
            }
            return ApiMetricsDataDTO.builder()
                    .totalRequests(v.getTotalRequests())
                    .totalResponses(v.getTotalResponses())
                    .failedRequests(v.getFailedRequests())
                    .cacheHits(v.getCacheHits() + 1)
                    .build();
        });
        Counter.builder("exchange.rate.cache.hits")
                .tag("api", apiName)
                .register(meterRegistry)
                .increment();
    }

    @Override
    public MetricsResponseDTO getMetrics() {
        List<ApiMetricsDTO> apis = metricsMap.entrySet().stream()
                .map(entry -> ApiMetricsDTO.builder()
                        .name(entry.getKey())
                        .metrics(entry.getValue())
                        .build())
                .collect(Collectors.toList());

        return MetricsResponseDTO.builder()
                .totalQueries(totalQueries.get())
                .apis(apis)
                .build();
    }

}