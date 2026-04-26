# Transaction Gateway Service – Project Flow Documentation

## Overview
This service acts as a gateway for payment transactions in a microservices architecture. It exposes REST APIs for payment initiation, processes business logic, interacts with downstream services (e.g., ledger, analytics), and integrates with infrastructure components like PostgreSQL, Redis, and Apache Kafka.

---

## High-Level Flow

1. **API Request Entry Point**
   - Client sends a POST request to `/v1/payments` with a `PaymentRequest` payload.
   - The request is handled by `PaymentController`.

2. **Controller Layer**
   - `PaymentController.createPayment()` receives the request.
   - Validates the request body using Jakarta Bean Validation (`@Valid`).
   - Delegates to `PaymentService.processPayment()` for business logic.
   - Handles exceptions and returns a `PaymentResponse` with status and message.

3. **Service Layer**
   - `PaymentService` contains the core business logic:
     - Validates business rules (e.g., duplicate transaction, sufficient balance, valid amount/currency).
     - Checks idempotency using Redis (prevents duplicate processing).
     - Persists transaction data to PostgreSQL via JPA repositories.
     - Publishes events to Kafka topics (e.g., payment-completed, payment-failed).
     - Handles retries, error scenarios, and downstream service communication (e.g., ledger service).

4. **Repository Layer**
   - Uses Spring Data JPA repositories to interact with PostgreSQL.
   - Handles CRUD operations for payment transactions.
   - Enforces constraints (e.g., unique transactionId, status updates).

5. **Kafka Integration**
   - **Producer:** Publishes payment events (success/failure) to Kafka topics.
   - **Consumer:** Listens for payment-related events from Kafka, updates DB, triggers further processing.
   - Handles malformed messages, retries, and dead-letter topics (DLT) for failed events.

6. **Redis Integration**
   - Used for idempotency checks (e.g., storing idempotency keys with TTL).
   - Prevents duplicate transaction processing.
   - Caches frequently accessed data if needed.

7. **Error Handling & Validation**
   - Handles validation errors (400), not found (404), and internal errors (500).
   - Returns meaningful error messages in `PaymentResponse`.
   - Implements retry logic for transient failures (e.g., downstream timeouts).

---

## Detailed Flow Example: Payment Creation

1. **Client** sends POST `/v1/payments` with JSON body:
   ```json
   {
     "transactionId": "TXN1001",
     "amount": 100.0,
     "currency": "USD",
     "receiverId": "user123"
   }
   ```
2. **PaymentController** validates and forwards to `PaymentService`.
3. **PaymentService**:
   - Checks for duplicate `transactionId` (idempotency via Redis).
   - Validates amount, currency, receiver existence, and balance.
   - Persists transaction as PENDING in PostgreSQL.
   - Calls downstream ledger service (may be REST or Kafka).
   - On success: updates status to SUCCESS, publishes `payment-completed` event to Kafka.
   - On failure: updates status to FAILED, publishes `payment-failed` event to Kafka.
   - Handles retries and DLT for Kafka failures.
4. **KafkaEventListener** (consumer):
   - Listens for payment events.
   - Updates DB status accordingly.
   - Handles malformed messages and retries.
5. **API Response**: Returns `PaymentResponse` with transactionId, status, and message.

---

## Error & Edge Case Handling
- **Duplicate transactionId:** Detected via Redis idempotency key.
- **Insufficient balance:** Returns failure response, does not persist transaction.
- **Invalid input:** Validation errors handled by controller/service.
- **Downstream service unavailable:** Retry logic, fallback, or failure response.
- **Kafka/DB/Redis failures:** Retries, error logging, DLT for Kafka.

---

## Infrastructure
- **PostgreSQL:** Stores all payment transactions and status updates.
- **Redis:** Idempotency, caching, duplicate prevention.
- **Kafka:** Event-driven communication for payment status and integration with other services.
- **Docker:** Containerized deployment for all components.

---

## Summary Table
| Layer         | Technology         | Responsibility                                  |
|---------------|--------------------|-------------------------------------------------|
| Controller    | Spring Web         | REST API, validation, error handling             |
| Service       | Spring Boot        | Business logic, orchestration, integration       |
| Repository    | Spring Data JPA    | DB persistence, constraints                      |
| Messaging     | Apache Kafka       | Event publishing/consuming, retries, DLT         |
| Caching       | Redis              | Idempotency, duplicate prevention                |
| Persistence   | PostgreSQL         | Transaction storage, ACID guarantees             |
| Container     | Docker             | Deployment, environment parity                   |

---

## Flow Diagram (Textual)

```
Client → PaymentController → PaymentService → [Redis, PostgreSQL, Kafka, Ledger Service]
         ↑                        ↓
      Response              KafkaEventListener (Consumer)
                                 ↓
                           Update DB, Retry, DLT
```

---

## How to Read the Code
- Start at `PaymentController` for API entry.
- Follow to `PaymentService` for business logic.
- Check repository classes for DB logic.
- See Kafka producer/consumer for event flows.
- Review Redis usage for idempotency.
- Integration tests (with Testcontainers) validate real infrastructure flows.

---

## Extending the Flow
- Add new payment types: Extend `PaymentService` and update controller.
- Add new events: Update Kafka producer/consumer logic.
- Add new DB fields: Update model, repository, and migration scripts.

---

## References
- [Spring Boot Docs](https://spring.io/projects/spring-boot)
- [Testcontainers](https://www.testcontainers.org/)
- [Kafka](https://kafka.apache.org/)
- [Redis](https://redis.io/)
- [PostgreSQL](https://www.postgresql.org/)

