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

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Iman Irajian
 * Date: 5/29/2025 1:23 AM
 */

class FrankfurterClientTest {

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

    FrankfurterClient frankfurterClient;

    private final String apiUrl = "http://fake-api";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(String.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        frankfurterClient = new FrankfurterClient(webClientBuilder, apiUrl);
    }

    @Test
    void getDataSourceName_returnsCorrectName() {
        assertEquals("Frankfurter API", frankfurterClient.getDataSourceName());
    }

    @Test
    void getExchangeRates_successfulResponse_returnsDTO() {
        // Prepare mock response body
        Map<String, Object> mockBody = Map.of(
                "rates", Map.of("EUR", 1.23, "GBP", 0.89)
        );

        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(mockBody));
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);

        Mono<ExchangeRateResponseDTO> result = frankfurterClient.getExchangeRates("USD", "EUR,GBP");

        StepVerifier.create(result)
                .assertNext(dto -> {
                    assertEquals("Frankfurter API", dto.getDatasource());
                    assertEquals("USD", dto.getBase());
                    assertNotNull(dto.getRates());
                    assertEquals(2, dto.getRates().size());
                    assertEquals(1.23, dto.getRates().get("EUR"));
                })
                .verifyComplete();

        // Verify correct URI called
        ArgumentCaptor<String> uriCaptor = ArgumentCaptor.forClass(String.class);
        verify(requestHeadersUriSpec).uri(uriCaptor.capture());
        String calledUri = uriCaptor.getValue();
        assertTrue(calledUri.contains("latest?from=USD"));
        assertTrue(calledUri.contains("to=EUR,GBP"));
    }

    @Test
    void getExchangeRates_responseHasNoRates_returnsEmptyRatesMap() {
        Map<String, Object> mockBody = Map.of("someOtherKey", "value");

        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(mockBody));
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);

        StepVerifier.create(frankfurterClient.getExchangeRates("USD", "EUR"))
                .assertNext(dto -> {
                    assertEquals("Frankfurter API", dto.getDatasource());
                    assertEquals("USD", dto.getBase());
                    assertNull(dto.getRates());
                })
                .verifyComplete();
    }

    @Test
    void getExchangeRates_httpError_returnsEmptyMono() {
        when(responseSpec.onStatus(any(), any())).thenAnswer(invocation -> {
            // simulate HTTP error onStatus trigger
            var predicate = invocation.getArgument(0, java.util.function.Predicate.class);
            if (predicate.test(HttpStatus.INTERNAL_SERVER_ERROR)) {
                return responseSpec; // normally would trigger error Mono, but for test return self
            }
            return responseSpec;
        });
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.error(new RuntimeException("API error")));

        StepVerifier.create(frankfurterClient.getExchangeRates("USD", "EUR"))
                .verifyComplete(); // should fallback to empty due to onErrorResume
    }

    @Test
    void getExchangeRates_networkError_returnsEmptyMono() {
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.error(new RuntimeException("Network error")));

        StepVerifier.create(frankfurterClient.getExchangeRates("USD", "EUR"))
                .verifyComplete(); // fallback to empty Mono on error
    }

}