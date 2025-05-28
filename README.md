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

✅ Smart Caching – Caches results using Redis

✅ Resilient Design – Circuit Breakers & reactive fallbacks

✅ Metrics Dashboard – Tracks API performance via Prometheus

✅ Production-Ready – Dockerized, validated inputs, and unit tested

## Technologies

- Java 21
- Spring Boot 3.4.6
- Spring WebFlux
- Spring Data Redis
- Resilience4j
- Micrometer
- Docker

## API Endpoints

- `GET /exchangeRates/{baseCur}?symbols={SYM1,SYM2...}` - Get exchange rates

> http://localhost:8080/exchangeRates/EUR?symbols=USD,NZD

- `GET /metrics` - Get service metrics
- `GET /actuator/prometheus` - Prometheus metrics endpoint

## Running the Application

### Prerequisites

- Java 21
- Maven
- Docker (for containerized deployment)

### Local Development

1. Start Redis:
   ```bash
   docker run -p 6379:6379 --name exchange-rates-redis -d redis:7.2
2. Or simply use Docker Compose to run everything:
   ```bash
   docker-compose up --build
   
---

### Author

Iman Irajian

Data Scientist | Full-Stack Developer

🔗 [LinkedIn](https://www.linkedin.com/in/imanirajian)

📧 Feel free to connect and reach out for collaboration or questions!