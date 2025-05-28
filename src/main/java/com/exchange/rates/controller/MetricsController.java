package com.exchange.rates.controller;

import com.exchange.rates.dto.MetricsResponseDTO;
import com.exchange.rates.service.MetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Iman Irajian
 * Date: 5/25/2025 09:20 PM
 */

@RestController
@RequestMapping("/metrics")
@RequiredArgsConstructor
public class MetricsController {

    private final MetricsService metricsService;

    @GetMapping
    public MetricsResponseDTO getMetrics() {
        return metricsService.getMetrics();
    }
}