package com.exchange.rates.service.client;

import com.exchange.rates.dto.ExchangeRateResponseDTO;
import reactor.core.publisher.Mono;

/**
 * @author Iman Irajian
 * Date: 5/25/2025 09:20 PM
 */

public interface ExchangeRateClient {
    String getDataSourceName();

    Mono<ExchangeRateResponseDTO> getExchangeRates(String baseCurrency, String symbols);
}