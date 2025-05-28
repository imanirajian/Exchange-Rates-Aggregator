package com.exchange.rates.service.client;

import com.exchange.rates.dto.ExchangeRateResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Iman Irajian
 * Date: 5/29/2025 1:25 AM
 */

class FreeCurrencyRatesClientTest {

    @Mock
    WebClient.Builder webClientBuilder;

    @Mock
    WebClient webClient;

    @Mock
    WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    WebClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    WebClient.ResponseSpec responseSpec;

    FreeCurrencyRatesClient freeCurrencyRatesClient;

    private final String apiUrl = "http://fake-api";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(String.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        freeCurrencyRatesClient = new FreeCurrencyRatesClient(webClientBuilder, apiUrl);
    }

    @Test
    void getDataSourceName_returnsCorrectName() {
        assertEquals("Free Currency Rates API", freeCurrencyRatesClient.getDataSourceName());
    }

    @Test
    void getExchangeRates_successfulResponse_filtersSymbolsCorrectly() {
        // Setup response body with base currency key in lowercase
        Map<String, Double> ratesMap = new HashMap<>();
        ratesMap.put("eur", 1.12);
        ratesMap.put("gbp", 0.85);
        ratesMap.put("jpy", 130.1);

        Map<String, Object> mockBody = Map.of("usd", ratesMap);

        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(mockBody));
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);

        Mono<ExchangeRateResponseDTO> result = freeCurrencyRatesClient.getExchangeRates("USD", "EUR,JPY");

        StepVerifier.create(result)
                .assertNext(dto -> {
                    assertEquals("Free Currency Rates API", dto.getDatasource());
                    assertEquals("USD", dto.getBase());
                    assertNotNull(dto.getRates());
                    assertEquals(2, dto.getRates().size());
                    assertEquals(1.12, dto.getRates().get("EUR"));
                    assertEquals(130.1, dto.getRates().get("JPY"));
                    assertFalse(dto.getRates().containsKey("GBP"));
                })
                .verifyComplete();

        // Verify correct URI called
        ArgumentCaptor<String> uriCaptor = ArgumentCaptor.forClass(String.class);
        verify(requestHeadersUriSpec).uri(uriCaptor.capture());
        String calledUri = uriCaptor.getValue();
        assertTrue(calledUri.endsWith("/currencies/usd.json"));
    }

    @Test
    void getExchangeRates_responseMissingBaseCurrencyKey_returnsDTOWithNullRates() {
        Map<String, Object> mockBody = Map.of("someOtherKey", Map.of("eur", 1.1));

        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(mockBody));
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);

        StepVerifier.create(freeCurrencyRatesClient.getExchangeRates("USD", "EUR"))
                .assertNext(dto -> {
                    assertEquals("Free Currency Rates API", dto.getDatasource());
                    assertEquals("USD", dto.getBase());
                    assertNull(dto.getRates());
                })
                .verifyComplete();
    }

    @Test
    void getExchangeRates_httpError_returnsEmptyMono() {
        when(responseSpec.onStatus(any(), any())).thenAnswer(invocation -> {
            var predicate = invocation.getArgument(0, java.util.function.Predicate.class);
            var function = invocation.getArgument(1, java.util.function.Function.class);
            if (predicate.test(HttpStatus.INTERNAL_SERVER_ERROR)) {
                return responseSpec;
            }
            return responseSpec;
        });
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.error(new RuntimeException("API error")));

        StepVerifier.create(freeCurrencyRatesClient.getExchangeRates("USD", "EUR"))
                .verifyComplete();
    }

    @Test
    void getExchangeRates_networkError_returnsEmptyMono() {
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.error(new RuntimeException("Network error")));

        StepVerifier.create(freeCurrencyRatesClient.getExchangeRates("USD", "EUR"))
                .verifyComplete();
    }

}