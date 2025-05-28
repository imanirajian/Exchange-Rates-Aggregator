package com.exchange.rates.service;

import com.exchange.rates.dto.MetricsResponseDTO;

/**
 * @author Iman Irajian
 * Date: 5/25/2025 09:20 PM
 */

public interface MetricsService {
    void incrementRequestCount(String apiName);

    void incrementResponseCount(String apiName);

    void incrementFailedCount(String apiName);

    void incrementCacheHit(String apiName);

    MetricsResponseDTO getMetrics();
}