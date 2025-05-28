package com.exchange.rates.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * @author Iman Irajian
 * Date: 5/25/2025 09:20 PM
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExchangeRateResponseDTO {
    private String datasource;
    private String base;
    private Map<String, Double> rates;
    private Instant timestamp;
}