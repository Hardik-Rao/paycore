# PayCore

Production-grade, UPI-inspired payment infrastructure — **Java 21 + Spring Boot 3.3** microservices.

## Services

| Service | Port | Description |
|---------|------|-------------|
| API Gateway | 8080 | JWT auth, rate limiting, routing |
| Payment | 8081 | Payments, accounts, admin dashboard |
| Ledger | 8082 | Double-entry ledger, balances |
| Fraud | 8083 | Rules engine, risk scoring |
| Notification | 8084 | Webhooks, SMS/email simulation |
| Reconciliation | 8085 | Daily reconciliation, disputes |

## Stack

PostgreSQL · Redis · Apache Kafka · Flyway · Docker · Prometheus · Grafana

## Quick Start

```bash
# Build all services
./gradlew build -x test

# Start full stack
docker compose up --build
```

- **Admin Dashboard:** http://localhost:8081/admin
- **Prometheus:** http://localhost:9090
- **Grafana:** http://localhost:3000 (admin/admin)

## Test Flow (direct to payment service)

```bash
# Create accounts
curl -X POST http://localhost:8081/api/v1/accounts \
  -H "Content-Type: application/json" \
  -d '{"vpa":"alice@paycore","accountHolder":"Alice","accountType":"INDIVIDUAL"}'

curl -X POST http://localhost:8081/api/v1/accounts \
  -H "Content-Type: application/json" \
  -d '{"vpa":"bob@paycore","accountHolder":"Bob","accountType":"MERCHANT"}'

# Initiate payment
curl -X POST http://localhost:8081/api/v1/payments \
  -H "Content-Type: application/json" \
  -d '{"idempotencyKey":"pay-001","payerVpa":"alice@paycore","payeeVpa":"bob@paycore","amount":500.00,"currency":"INR"}'

# Check ledger balance
curl http://localhost:8082/api/v1/ledger/accounts/bob@paycore/balance
```

## Via API Gateway

Gateway requires JWT Bearer token. Generate a test token with HS256 secret `paycore-dev-secret-key-change-in-production-32chars`.

## Repo

https://github.com/Hardik-Rao/paycore
