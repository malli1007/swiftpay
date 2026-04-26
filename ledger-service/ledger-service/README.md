# Ledger Service - SwiftPay

## Overview

The Ledger Service is responsible for processing money transfers, maintaining account balances, storing transaction history, and publishing final payment results to Kafka topics.

It acts as the core accounting engine of the SwiftPay microservices system.

---

# High Level Flow

1. Transaction Gateway publishes `payment-initiated` event to Kafka.
2. Ledger Service consumes the event.
3. Validates sender and receiver accounts.
4. Debits sender balance.
5. Credits receiver balance.
6. Saves ledger transaction in database.
7. Publishes success/failure event back to Kafka.
8. Other services (Analytics, Gateway) consume result.

---

# Project Components

## Configuration Layer

### KafkaConfig

Configures Kafka producer and consumer beans.

### Producer Beans

Used to publish messages:

* `payment-completed`
* `payment-failed`
* Dead Letter Topics (DLT)

### Consumer Beans

Consumes:

* `payment-initiated`

### Error Handling

If message processing fails:

* Retries 3 times
* Waits 2 seconds between retries
* Sends failed message to `<topic>.DLT`

---

### RedisConfig

Creates Redis connection and RedisTemplate.

Currently available for caching/idempotency extension if needed.

---

### OpenApiConfig

Swagger/OpenAPI configuration.

Provides API docs for testing endpoints.

---

# Controller Layer

## LedgerController

Base URL:
`/v1/transactions`

---

## API 1: Get Transaction History

### Endpoint

`GET /v1/transactions/{userId}`

### What It Does

Returns all transactions where user is sender or receiver.

### Example

For userId=1:

* Sent money to others
* Received money from others

### Data Source

`ledger_transactions` table

---

## API 2: Get Account Balance

### Endpoint

`GET /v1/transactions/accounts/{userId}/balance`

### What It Does

Returns current balance of account.

If account not found returns `0`.

### Data Source

`accounts` table

---

# Entity Layer

## Account Entity

Maps to table: `accounts`

Columns:

* userId (Primary Key)
* balance

Stores current wallet balance for each user.

---

## LedgerTransaction Entity

Maps to table: `ledger_transactions`

Columns:

* id
* transactionId
* senderId
* receiverId
* amount
* status
* createdAt

Stores transfer history.

---

# Kafka Listener Layer

## KafkaEventListener

Consumes Kafka events.

### Listener 1

Topic: `payment-initiated`

When message arrives:

* Logs event
* Calls `ledgerService.processTransfer()`

### Listener 2

Topic: `payment-initiated.DLT`

If message failed after retries:

* Logs error
* No further processing

---

# Model Classes

## PaymentInitiatedEvent

Input event from Gateway.

Fields:

* transactionId
* senderId
* receiverId
* amount
* currency

---

## PaymentCompletedEvent

Success output event.

Fields:

* transactionId
* status
* message

---

## PaymentFailedEvent

Failure output event.

Fields:

* transactionId
* message

---

# Repository Layer

## AccountRepository

Used for:

* Find account by userId
* Save updated balances

---

## LedgerTransactionRepository

Used for:

* Save transaction history
* Find transactions by sender/receiver

---

# Service Layer

## LedgerService

Main business logic class.

## Method: processTransfer(String message)

This method processes transfer event from Kafka.

---

# Step-by-Step Internal Flow

## Step 1: Read Kafka JSON Message

Incoming JSON converted into:
`PaymentInitiatedEvent`

Example:

```json
{
  "transactionId":"TXN1001",
  "senderId":1,
  "receiverId":2,
  "amount":500,
  "currency":"INR"
}
```

---

## Step 2: Load Sender Account

Search sender in `accounts` table.

If sender not found:

* Exception thrown
* Retry/DLT flow triggered by Kafka handler

---

## Step 3: Load Receiver Account

Search receiver in database.

If receiver not found:

* Publish `payment-failed`
* Message: RECEIVER NOT FOUND
* Stop processing

---

## Step 4: Debit Sender

Subtract amount from sender balance.

Example:

* Before: 1000
* Amount: 500
* After: 500

---

## Step 5: Credit Receiver

Add amount to receiver balance.

Example:

* Before: 200
* Amount: 500
* After: 700

---

## Step 6: Save Updated Accounts

Both balances stored in database.

---

## Step 7: Save Ledger Transaction

Insert row into `ledger_transactions`.

Status = SUCCESS

---

## Step 8: Publish Success Event

Send to Kafka topic:
`payment-completed`

Payload:

```json
{
  "transactionId":"TXN1001",
  "status":"SUCCESS",
  "message":"Transferred successfully"
}
```

---

# Kafka Topics Used

## Input Topic

* `payment-initiated`

## Output Topics

* `payment-completed`
* `payment-failed`

## Dead Letter Topic

* `payment-initiated.DLT`

---

# Database Tables

## accounts

Stores wallet balances.

## ledger_transactions

Stores transaction history.

---

# Example End-to-End Scenario

## Initial Data

User1 balance = 1000
User2 balance = 200

## Request

Transfer 500 from User1 to User2

## Result

User1 = 500
User2 = 700

Transaction saved.
Kafka success event published.

---

# Failure Scenario

If receiver does not exist:

* No credit
* No transaction history success row
* Publish `payment-failed`

---

# Why This Service Is Important

This service guarantees:

* Balance consistency
* Transfer recording
* Reliable event processing
* Audit history
* Decoupled microservice communication

---

# Tech Stack

* Java 21
* Spring Boot
* Spring Data JPA
* PostgreSQL
* Kafka
* Redis
* Swagger/OpenAPI
* JUnit / Mockito / Testcontainers

---

# Author

SwiftPay Microservices Project
