# PayCore

Production-grade, UPI-inspired payment infrastructure built with **Java 21** and **Spring Boot 3.3** microservices.

## Architecture

- **API Gateway** (8080) — JWT auth, rate limiting, routing
- **Payment Service** (8081) — payments, accounts, state machine, idempotency
- **Ledger Service** (8082) — double-entry ledger, balances
- **Fraud Service** (8083) — rules engine, risk scoring
- **Notification Service** (8084) — webhooks, SMS/email simulation
- **Reconciliation Service** (8085) — daily reconciliation, disputes

## Stack

PostgreSQL · Redis · Apache Kafka · Flyway · Docker · Prometheus · Grafana

## Quick Start

```bash
docker compose up --build
```

## Prerequisites

- Java 21
- Docker & Docker Compose

## Status

?? Under active development — Week 1 foundation in progress.
