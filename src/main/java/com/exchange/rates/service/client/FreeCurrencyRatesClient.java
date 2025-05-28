package com.exchange.rates.service.client;

import com.exchange.rates.dto.ExchangeRateResponseDTO;
import com.exchange.rates.exception.ExchangeRateException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Iman Irajian
 * Date: 5/25/2025 09:20 PM
 */

@Log4j2
@Component
public class FreeCurrencyRatesClient implements ExchangeRateClient {

    private final WebClient webClient;
    private final String apiUrl;

    public FreeCurrencyRatesClient(WebClient.Builder webClientBuilder,
                                   @Value("${exchange.api.free-currency-rates.url}") String apiUrl) {
        this.webClient = webClientBuilder.build();
        this.apiUrl = apiUrl;
    }

    @Override
    public String getDataSourceName() {
        return "Free Currency Rates API";
    }

    @Override
    public Mono<ExchangeRateResponseDTO> getExchangeRates(String baseCurrency, String symbols) {
        String url = String.format("%s/currencies/%s.json", apiUrl, baseCurrency.toLowerCase());

        return webClient.get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> {
                    log.error("Error fetching rates from Free Currency Rates API: {}", response.statusCode());
                    return Mono.error(new ExchangeRateException("Failed to fetch rates from Free Currency Rates API"));
                })
                .bodyToMono(Map.class)
                .map(response -> {
                    ExchangeRateResponseDTO.ExchangeRateResponseDTOBuilder builder = ExchangeRateResponseDTO.builder()
                            .datasource(getDataSourceName())
                            .base(baseCurrency);

                    if (response != null && response.containsKey(baseCurrency.toLowerCase())) {
                        Map<String, Double> ratesMap = (Map<String, Double>) response.get(baseCurrency.toLowerCase());
                        if (symbols != null) {
                            Map<String, Double> filteredRates = new HashMap<>();
                            String[] symbolsArray = symbols.split(",");
                            for (String symbol : symbolsArray) {
                                String symbolLower = symbol.toLowerCase();
                                if (ratesMap.containsKey(symbolLower)) {
                                    filteredRates.put(symbol, ratesMap.get(symbolLower));
                                }
                            }
                            builder.rates(filteredRates);
                        }
                    }
                    log.info("FreeCurrencyRatesClient called with response: {}", response);
                    return builder.build();
                })
                .doOnError(e -> log.error("Error in FreeCurrencyRatesClient: {}", e.getMessage()))
                .onErrorResume(e -> Mono.empty());
    }
}