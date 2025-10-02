# currency-rate-service

Reactive Spring Boot service that fetches fiat & crypto rates, stores them in PostgreSQL (R2DBC), and exposes them via GET /currency-rates.

# Tech

Java 21 · Spring Boot 3.5 · WebFlux · WebClient · R2DBC · PostgreSQL · Flyway · Actuator

# Quick start

## 1) Start Postgres

docker run --name currencydb
-e POSTGRES_DB=currencydb -e POSTGRES_USER=currency -e POSTGRES_PASSWORD=currency
-p 5432:5432 -d postgres:16

## 2) Optional overrides

export RATES_FIAT_BASE_URL=http://localhost:8080/fiat-currency-rates

export RATES_FIAT_API_HEADER=X-API-KEY
export RATES_FIAT_API_KEY=secret-key
export RATES_CRYPTO_BASE_URL=http://localhost:8080/crypto-currency-rates

export RATES_HTTP_TIMEOUT=3s

## 3) Run the app

./gradlew bootRun

### API
GET /currency-rates

### Returns:
{ "fiat": [ { "currency": "USD", "rate": 70.12 } ], "crypto": [ { "currency": "BTC", "rate": 88949.97 } ] }

### Health
GET /actuator/health

### Tests
./gradlew test