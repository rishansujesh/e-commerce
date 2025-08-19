# Event-Driven E-Commerce Backend

This repository contains a production-style, event-driven microservices backend for a small e-commerce platform.  It demonstrates Spring Boot 3.3, Java 21, Kafka, PostgreSQL, Redis and Testcontainers working together with the Outbox pattern and saga-style choreography.

## Features

* **Four microservices**: `catalog`, `orders`, `payments`, and `shipping`, each as an independent Spring Boot application.
* **Event-driven architecture** using Kafka topics:
  * `order.created`, `order.confirmed`, `order.cancelled`
  * `payment.authorized`, `payment.captured`, `payment.failed`
  * `shipment.scheduled`, `shipment.dispatched`
* **Outbox pattern**: state changes and domain events written in the same transaction and later published to Kafka.
* **Saga choreography**: services react to each other's events to drive the order lifecycle.
* **PostgreSQL** for persistent storage and **Flyway** for schema migrations.
* **Redis** caching for hot lookups (product information and order summaries).
* **Docker Compose** environment for local development.
* **Integration tests** powered by Testcontainers.
* **GitHub Actions** CI running the Maven build and tests.

## Repository Layout

```
ecommerce-backend/
  .github/workflows/ci.yml
  .env.example
  docker-compose.yml
  pom.xml
  scripts/
  services/
```

Each service has its own Maven module under `services/` with `application.yml`, database migrations and tests.

## Prerequisites

* Docker & Docker Compose v2
* JDK 21
* Maven 3.9+

## Getting Started

1. **Clone and configure environment**
   ```bash
   cp .env.example .env
   ```
2. **Start infrastructure and services**
   ```bash
   docker compose up -d
   scripts/create-topics.sh
   ```
   Kafka, Redis, and PostgreSQL containers will start along with the service images once built.
3. **Build the code**
   ```bash
   mvn -q -DskipTests package
   ```
4. **Run sample workflow**
   ```bash
   scripts/curl-samples.sh
   ```
   The script creates products, places an order, polls status, and cancels an order.

## Testing

Run all unit and integration tests:
```bash
mvn -q -DskipITs=false verify
```
Testcontainers automatically starts Kafka, PostgreSQL, and Redis containers for the tests.

## CI

GitHub Actions workflow (`.github/workflows/ci.yml`) builds the project, caches dependencies, and executes the full Maven verification phase on every push.
