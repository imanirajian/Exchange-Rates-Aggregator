# Exchange Rates Aggregator Microservice 

A high-performance Spring Boot microservice that fetches, caches, and aggregates currency exchange rates from multiple APIs (Frankfurter, FreeCurrencyRates). Features real-time rate averaging, resilient fault tolerance (Circuit Breakers), Prometheus metrics, and Docker support.

[![Java](https://img.shields.io/badge/Java-21-blue)](https://openjdk.org/projects/jdk/21/)  
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.6-brightgreen)](https://spring.io/projects/spring-boot)  

A resilient microservice that aggregates currency exchange rates from multiple APIs, caches results, and exposes metrics for monitoring.  

## Use Cases  
- Financial apps needing real-time forex data  
- Multi-provider rate comparison  
- Demo for Spring Boot best practices (Caching, Circuit Breakers, Metrics)

## Key Features:
✅ Multi-Provider Aggregation – Combines rates from multiple APIs for accuracy

✅ Smart Caching – 30-minute TTL to reduce external API calls

✅ Resilient Design – Circuit Breakers & reactive fallbacks

✅ Metrics Dashboard – Tracks API performance via Prometheus

✅ Production-Ready – Dockerized, validated inputs, and unit tested
