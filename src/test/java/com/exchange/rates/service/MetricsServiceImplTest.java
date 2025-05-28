package com.exchange.rates.service;

import com.exchange.rates.dto.ApiMetricsDTO;
import com.exchange.rates.dto.ApiMetricsDataDTO;
import com.exchange.rates.dto.MetricsResponseDTO;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Iman Irajian
 * Date: 5/25/2025 09:20 PM
 */

class MetricsServiceImplTest {

    MetricsServiceImpl metricsService;

    @BeforeEach
    void setUp() {
        metricsService = new MetricsServiceImpl(new SimpleMeterRegistry());
    }

    @Test
    void testIncrementRequestCount() {
        metricsService.incrementRequestCount("api1");

        MetricsResponseDTO metrics = metricsService.getMetrics();
        ApiMetricsDTO apiMetrics = metrics.getApis().get(0);

        assertThat(apiMetrics.getName()).isEqualTo("api1");
        assertThat(apiMetrics.getMetrics().getTotalRequests()).isEqualTo(1);
        assertThat(metrics.getTotalQueries()).isEqualTo(1);
    }

    @Test
    void testIncrementResponseCount() {
        metricsService.incrementRequestCount("api1");
        metricsService.incrementResponseCount("api1");

        ApiMetricsDataDTO data = metricsService.getMetrics().getApis().get(0).getMetrics();

        assertThat(data.getTotalResponses()).isEqualTo(1);
        assertThat(data.getTotalRequests()).isEqualTo(1);
    }

    @Test
    void testIncrementFailedCount() {
        metricsService.incrementRequestCount("api1");
        metricsService.incrementFailedCount("api1");

        ApiMetricsDataDTO data = metricsService.getMetrics().getApis().get(0).getMetrics();

        assertThat(data.getFailedRequests()).isEqualTo(1);
    }

    @Test
    void testIncrementCacheHit() {
        metricsService.incrementCacheHit("api1");

        ApiMetricsDataDTO data = metricsService.getMetrics().getApis().get(0).getMetrics();

        assertThat(data.getCacheHits()).isEqualTo(1);
    }

    @Test
    void testMultipleMetricsAggregation() {
        metricsService.incrementRequestCount("api1");
        metricsService.incrementRequestCount("api1");
        metricsService.incrementResponseCount("api1");
        metricsService.incrementCacheHit("api1");
        metricsService.incrementFailedCount("api1");

        ApiMetricsDataDTO data = metricsService.getMetrics().getApis().get(0).getMetrics();

        assertThat(data.getTotalRequests()).isEqualTo(2);
        assertThat(data.getTotalResponses()).isEqualTo(1);
        assertThat(data.getFailedRequests()).isEqualTo(1);
        assertThat(data.getCacheHits()).isEqualTo(1);
    }

    @Test
    void shouldIncrementCountersAndReturnMetrics() {
        metricsService.incrementRequestCount("API1");
        metricsService.incrementRequestCount("API1");
        metricsService.incrementResponseCount("API1");
        metricsService.incrementFailedCount("API1");
        metricsService.incrementCacheHit("API1");

        metricsService.incrementRequestCount("API2");
        metricsService.incrementResponseCount("API2");

        MetricsResponseDTO response = metricsService.getMetrics();

        assertEquals(3, response.getTotalQueries());
        assertEquals(2, response.getApis().size());

        ApiMetricsDTO api1Metrics = response.getApis().stream()
                .filter(api -> api.getName().equals("API1"))
                .findFirst()
                .orElseThrow();

        assertEquals(2, api1Metrics.getMetrics().getTotalRequests());
        assertEquals(1, api1Metrics.getMetrics().getTotalResponses());
        assertEquals(1, api1Metrics.getMetrics().getFailedRequests());
        assertEquals(1, api1Metrics.getMetrics().getCacheHits());
    }
}