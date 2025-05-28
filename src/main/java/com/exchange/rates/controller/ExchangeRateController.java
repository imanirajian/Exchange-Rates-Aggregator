package com.exchange.rates.controller;

import com.exchange.rates.dto.ExchangeRateResponseDTO;
import com.exchange.rates.service.ExchangeRateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @author Iman Irajian
 * Date: 5/25/2025 09:20 PM
 */

@RestController
@RequiredArgsConstructor
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    @GetMapping("/exchangeRates/{baseCur}")
    public Mono<ResponseEntity<ExchangeRateResponseDTO>> getExchangeRates(
            @PathVariable String baseCur,
            @RequestParam(required = false) String symbols) {
        return exchangeRateService.getExchangeRates(baseCur, symbols)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
