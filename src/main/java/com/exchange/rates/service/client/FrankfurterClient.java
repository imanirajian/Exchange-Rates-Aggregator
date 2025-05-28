package com.exchange.rates.service.client;

import com.exchange.rates.dto.ExchangeRateResponseDTO;
import com.exchange.rates.exception.ExchangeRateException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * @author Iman Irajian
 * Date: 5/25/2025 09:20 PM
 */

@Log4j2
@Component
public class FrankfurterClient implements ExchangeRateClient {

    private final WebClient webClient;
    private final String apiUrl;

    public FrankfurterClient(WebClient.Builder webClientBuilder,
                             @Value("${exchange.api.frankfurter.url}") String apiUrl) {
        this.webClient = webClientBuilder.build();
        this.apiUrl = apiUrl;
    }

    @Override
    public String getDataSourceName() {
        return "Frankfurter API";
    }

    @Override
    public Mono<ExchangeRateResponseDTO> getExchangeRates(String baseCurrency, String symbols) {
        String url = String.format("%s/latest?from=%s", apiUrl, baseCurrency);
        if (symbols != null && !symbols.isEmpty()) {
            url += "&to=" + symbols.replace(",", ",");
        }

        return webClient.get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> {
                    log.error("Error fetching rates from Frankfurter API: {}", response.statusCode());
                    return Mono.error(new ExchangeRateException("Failed to fetch rates from Frankfurter API"));
                })
                .bodyToMono(Map.class)
                .map(response -> {
                    ExchangeRateResponseDTO.ExchangeRateResponseDTOBuilder builder = ExchangeRateResponseDTO.builder()
                            .datasource(getDataSourceName())
                            .base(baseCurrency);

                    if (response != null && response.containsKey("rates")) {
                        Map<String, Double> rates = (Map<String, Double>) response.get("rates");
                        builder.rates(rates);
                    }
                    log.info("FrankfurterClient called with response: {}", response);
                    return builder.build();
                })
                .doOnError(e -> log.error("Error in FrankfurterClient: {}", e.getMessage()))
                .onErrorResume(e -> Mono.empty());
    }
}