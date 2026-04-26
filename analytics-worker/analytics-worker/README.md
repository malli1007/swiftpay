# Analytics Worker - Controller Flow and Application Overview

## Overview
The Analytics Worker service is responsible for consuming payment event messages from Kafka topics, processing them, and storing analytics records in the database. It also exposes REST APIs to fetch analytics data.

## Controller Flow

### 1. Kafka Listeners
- **PaymentCompletedListener** listens to two main topics:
  - `payment-completed`: On receiving a message, it calls `AnalyticsService.saveAnalytics(message)`.
  - `payment-failed`: On receiving a message, it calls `AnalyticsService.saveFailedAnalytics(message)`.
  - DLT (Dead Letter Topics): Logs messages that could not be processed.

### 2. Service Layer
- **AnalyticsService** handles the business logic:
  - `saveAnalytics(String message)`: Parses the message as a `PaymentCompletedEvent`, creates an `AnalyticsRecord`, and saves it to the database.
  - `saveFailedAnalytics(String message)`: Parses the message as a `PaymentFailedEvent`, creates an `AnalyticsRecord`, and saves it to the database.

### 3. Repository Layer
- **AnalyticsRepository** is a Spring Data JPA repository for `AnalyticsRecord` entities. It provides methods like `findAll()` and `count()`.

### 4. REST Controller
- **AnalyticsController** exposes two endpoints:
  - `GET /v1/analytics`: Returns all analytics records.
  - `GET /v1/analytics/count`: Returns the total count of analytics records.

## Data Model
- **AnalyticsRecord**: Entity representing an analytics record with fields:
  - `id` (Long, auto-generated)
  - `transactionId` (String)
  - `status` (String)
  - `processedAt` (LocalDateTime)

## Flow Summary
1. **Event Reception**: Kafka listener receives a payment event message.
2. **Processing**: The message is parsed and mapped to an entity in the service layer.
3. **Persistence**: The entity is saved to the database via the repository.
4. **API Access**: Analytics data can be retrieved using the REST API endpoints.

## Example Sequence
1. A payment completes or fails in the system, and a message is published to Kafka.
2. The Analytics Worker consumes the message and saves a record.
3. Users can query analytics data via the provided REST endpoints.

---

For more details, see the source files:
- `controller/AnalyticsController.java`
- `service/AnalyticsService.java`
- `kafka/PaymentCompletedListener.java`
- `entity/AnalyticsRecord.java`
- `repository/AnalyticsRepository.java`

