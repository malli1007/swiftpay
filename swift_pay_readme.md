# SwiftPay - Real-Time Payment Ledger

## Overview
SwiftPay is a microservices-based fintech system for peer-to-peer payments. It demonstrates event-driven architecture using Spring Boot, Kafka, PostgreSQL, Redis, and Docker.

## Architecture
- **Service A:** Transaction Gateway
- **Service B:** Ledger Service
- **Service C:** Analytics Worker

## Tech Stack
- Java 21
- Spring Boot
- PostgreSQL
- Apache Kafka
- Redis
- Docker / Docker Compose
- Maven

## Project Structure
```text
swiftpay/
├── transaction-gateway-service/
├── ledger-service/
├── analytics-worker/
└── docker-compose.yml
```

## Functional Flow
1. Client calls `POST /v1/payments` on Service A.
2. Service A validates request, checks Redis idempotency, verifies balance, stores PENDING record, publishes `payment-initiated`.
3. Service B consumes event, performs atomic debit/credit, stores ledger transaction, publishes `payment-completed` or `payment-failed`.
4. Service C consumes completed events and stores analytics records.

## APIs
### Service A
- `POST /v1/payments`

### Service B
- `GET /v1/transactions/{userId}`
- `GET /v1/accounts/{userId}/balance`

### Service C
- `GET /v1/analytics`
- `GET /v1/analytics/count`

## Run Locally
### Prerequisites
- Java 21
- Maven
- Docker Desktop

### Start Infrastructure
```bash
docker compose up -d
```

### Build Services
```bash
mvn clean package
```

### Run Services
Run each Spring Boot application:
- TransactionGatewayServiceApplication
- LedgerServiceApplication
- AnalyticsWorkerApplication

## Sample Request
```json
POST /v1/payments
{
  "transactionId": "TXN1001",
  "senderId": 1,
  "receiverId": 2,
  "amount": 500,
  "currency": "INR"
}
```

## Sample Test Data
```sql
insert into accounts(user_id,balance) values (1,10000);
insert into accounts(user_id,balance) values (2,5000);
```

## Key Features
- Redis idempotency (24h)
- Event-driven communication with Kafka
- Transaction-safe balance transfer
- Reporting APIs
- Analytics ingestion

## Future Enhancements
- Swagger/OpenAPI
- Kafka Retry / DLQ
- Kubernetes manifests
- GitHub Actions CI/CD
- ClickHouse integration

## Author
SwiftPay Hackathon Submission

