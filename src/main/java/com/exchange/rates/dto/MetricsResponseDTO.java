package com.exchange.rates.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Iman Irajian
 * Date: 5/25/2025 09:20 PM
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricsResponseDTO {
    private long totalQueries;
    private List<ApiMetricsDTO> apis;
}