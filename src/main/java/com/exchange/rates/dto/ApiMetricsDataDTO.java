package com.exchange.rates.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Iman Irajian
 * Date: 5/25/2025 09:20 PM
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiMetricsDataDTO {
    private long totalRequests;
    private long totalResponses;
    private long failedRequests;
    private long cacheHits;
}