# Uptime & Reliable Webhook Monitor

![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.13-FF6600?logo=rabbitmq&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?logo=docker&logoColor=white)

A backend API uptime monitoring and event-driven webhook delivery engine built with **Java 21** and **Spring Boot**. Designed to go beyond standard CRUD patterns, it solves real-world production challenges: concurrent job scheduling, false-positive filtering, cryptographic payload signing, asynchronous delivery with exponential backoff, and dead-letter recovery.

---

## Key Features

- **Concurrent Uptime Probing:** Schedules periodic HTTP health checks using **Java 21 Virtual Threads** and `RestClient` with explicit connect/read timeouts.
- **Horizontally Safe Scheduling:** Claims due monitors atomically in PostgreSQL using `FOR UPDATE SKIP LOCKED`, preventing duplicate checks across multiple application replicas.
- **Anti-Spam State Machine:** Filters transient network blips by requiring consecutive failures (configurable, default `2`) before transitioning `UP → DOWN`, while notifying `DOWN → UP` immediately on recovery.
- **Signed Webhooks (HMAC-SHA256):** Signs every outgoing payload with `HMAC-SHA256(timestamp + "." + rawPayload, secretKey)` and attaches `X-Webhook-Timestamp`, `X-Webhook-Delivery-Id`, and `X-Webhook-Signature` headers to prevent tampering and replay attacks.
- **Resilient Asynchronous Delivery:** Decouples probing from notification via **RabbitMQ** with tiered **exponential backoff retry queues** + randomized jitter, smart error classification (`5xx`/`429`/timeouts retry; `4xx` fail fast), and a **Dead Letter Queue (DLQ)**.
- **Operational Control:** Exposes delivery audit logs and a `POST /api/deliveries/{id}/retry` endpoint to replay failed deliveries from the DLQ.
- **Production-Grade Defaults:** Schema-first migrations with **Flyway** (`ddl-auto: validate`), RFC 7807 `ProblemDetail` error responses, OpenAPI/Swagger documentation, and automated E2E tests running on real containers.

---

### Modular Monolith Layout

Packages are structured by **business domain** rather than global technical layers. Repositories and internal helpers use **package-private visibility** so the compiler enforces strict boundaries between modules:

```text
src/main/java/com/uptime/
├── monitor/   # Monitor CRUD, SKIP LOCKED claimer, Virtual Thread probe, state machine
├── webhook/   # Subscriptions, RabbitMQ topology, HMAC-SHA256 signer, retry worker, DLQ replay
└── shared/    # Cross-cutting RFC 7807 exception handling and OpenAPI config
```

---

## Architectural & Engineering Decisions

| Decision | Why It Was Chosen |
|---|---|
| **Modular Monolith over Microservices** | Keeps deployment simple and avoids premature distributed overhead while enforcing clean module boundaries via package-private access and Spring `ApplicationEvent` decoupling. |
| **3-Phase Check Execution (No HTTP inside DB Transactions)** | Claiming due monitors, running external HTTP calls, and persisting results run in isolated phases so slow remote endpoints never starve the HikariCP connection pool. |
| **`FOR UPDATE SKIP LOCKED`** | Allows multiple instances to poll due monitors concurrently without row contention or duplicate webhook alerts. |
| **`TEXT` instead of `JSONB` for `payload_json`** | PostgreSQL `JSONB` normalizes whitespace and reorders object keys. Storing raw `TEXT` guarantees byte-for-byte payload immutability across retries so HMAC signatures remain consistent. |
| **Tiered Retry Queues (`retry.1` – `retry.4`)** | RabbitMQ evaluates per-message TTL only at the head of a queue. Separating backoff intervals into dedicated TTL queues eliminates head-of-line blocking without external broker plugins. |
| **`AFTER_COMMIT` Event Publishing + ID-Only Messages** | Publishes only the `deliveryId` after PostgreSQL commits the transaction, avoiding race conditions where the worker reads an uncommitted row and keeping PostgreSQL as the single source of truth. |
| **Secret Key Shown Once & Constant-Time Verification** | Generates 256-bit keys with `SecureRandom`, returns them exclusively on `POST` creation, and verifies signatures using `MessageDigest.isEqual` to prevent timing attacks. |
| **`open-in-view: false` & `ddl-auto: validate`** | Prevents hidden N+1 queries during JSON serialization and guarantees Hibernate entities stay strictly aligned with Flyway SQL migrations. |

---

## Quick Start

### Prerequisites
- **Docker** & **Docker Compose**
- *(Optional for local dev)* **Java 21** (Maven Wrapper `./mvnw` is included)

### Option 1: Run the Full Stack with Docker (1 Command)

```bash
git clone [https://github.com/PedroUchoa/Webhooks-Alerts]
cd WebHooksAlerts
docker compose up --build
```

### Option 2: Local Development Mode (IDE + Docker Infra)

```bash
# Start PostgreSQL and RabbitMQ only
docker compose up -d postgres rabbitmq

# Run the Spring Boot app locally
./mvnw spring-boot:run
```

### Useful Local URLs
- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI Spec:** `http://localhost:8080/v3/api-docs`
- **RabbitMQ Management UI:** `http://localhost:15672` (`guest` / `guest`)

---

## API Reference

| Method | Endpoint | Description | Status |
|---|---|---|---|
| `POST` | `/api/monitors` | Register a new URL monitor | `201 Created` |
| `GET` | `/api/monitors` | List all monitors | `200 OK` |
| `GET` | `/api/monitors/{id}` | Get monitor details | `200 OK` |
| `PATCH` | `/api/monitors/{id}/status` | Pause or resume monitoring (`active`) | `200 OK` |
| `DELETE` | `/api/monitors/{id}` | Delete monitor and its subscriptions | `204 No Content` |
| `GET` | `/api/monitors/{id}/executions` | List last 50 health check executions | `200 OK` |
| `POST` | `/api/monitors/{id}/webhooks` | Create webhook subscription (returns `secretKey` once) | `201 Created` |
| `GET` | `/api/monitors/{id}/webhooks` | List subscriptions (omits `secretKey`) | `200 OK` |
| `DELETE` | `/api/monitors/{id}/webhooks/{webhookId}` | Remove a webhook subscription | `204 No Content` |
| `GET` | `/api/monitors/{id}/deliveries` | Audit log of the last 50 webhook deliveries | `200 OK` |
| `POST` | `/api/deliveries/{deliveryId}/retry` | Manually replay a `FAILED` delivery | `202 Accepted` |

---

## Usage Walkthrough (`curl`)

### 1. Create a Monitor

```bash
curl -s -X POST http://localhost:8080/api/monitors \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Payments API",
    "targetUrl": "[https://httpbin.org/status/503](https://httpbin.org/status/503)",
    "intervalSeconds": 30,
    "timeoutMs": 5000
  }'
```

### 2. Subscribe a Webhook Endpoint

Use a temporary inbox from [webhook.site](https://webhook.site) or your own callback URL:

```bash
curl -s -X POST http://localhost:8080/api/monitors/{MONITOR_ID}/webhooks \
  -H "Content-Type: application/json" \
  -d '{
    "callbackUrl": "[https://webhook.site/your-unique-id](https://webhook.site/your-unique-id)",
    "events": ["MONITOR_DOWN", "MONITOR_RECOVERED"]
  }'
```

Save the `secretKey` returned in the response — it is omitted from all subsequent `GET` requests.

### 3. Inspect Deliveries & Replay Failed Events

```bash
# View delivery attempts, HTTP statuses, and error details
curl -s http://localhost:8080/api/monitors/{MONITOR_ID}/deliveries

# Manually requeue a FAILED delivery from the DLQ
curl -i -X POST http://localhost:8080/api/deliveries/{DELIVERY_ID}/retry
```

---

## Webhook Payload & Signature Verification

Every outgoing `POST` includes these headers:

| Header | Example | Purpose |
|---|---|---|
| `X-Webhook-Delivery-Id` | `9c8e1d7a-...` | Unique delivery UUID for receiver-side idempotency |
| `X-Webhook-Timestamp` | `1764000000` | Unix epoch timestamp (seconds) for anti-replay checks |
| `X-Webhook-Signature` | `sha256=3f9a...` | Hex-encoded HMAC-SHA256 digest |

**Example Payload:**

```json
{
  "eventId": "f3b7c2d0-4a8e-4e92-8b3d-2c7d91f4e1a0",
  "eventType": "MONITOR_DOWN",
  "monitorId": "2e6c5a1b-8c4f-41f9-a61e-9a4e73b2d1f4",
  "monitorName": "Payments API",
  "targetUrl": "[https://httpbin.org/status/503](https://httpbin.org/status/503)",
  "httpStatus": 503,
  "latencyMs": 142,
  "errorMessage": "Status HTTP inesperado: 503",
  "occurredAt": "2026-11-26T22:00:00Z"
}
```

**Verifying a Signature with OpenSSL:**

Consumers should reject timestamps older than a tolerance window (e.g., 5 minutes) and compare the computed digest in constant time:

```bash
printf "%s.%s" "<X-Webhook-Timestamp>" '<RAW_JSON_BODY>' \
  | openssl dgst -sha256 -hmac "<SECRET_KEY>"
```



## Tech Stack

- **Language & Framework:** Java 21 (Virtual Threads, Records), Spring Boot 3, Spring Web (`RestClient`), Spring Data JPA, Spring AMQP, Spring Validation
- **Database & Migrations:** PostgreSQL 16, Flyway
- **Messaging:** RabbitMQ 3.13 (Direct Exchange, TTL Retry Queues, Dead Letter Queue)
- **Documentation:** SpringDoc OpenAPI 3 / Swagger UI
- **Testing & CI/CD:** JUnit 5, AssertJ, Testcontainers, WireMock, Awaitility, Multi-stage Docker, GitHub Actions