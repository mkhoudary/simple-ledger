# SimpleLedger

SimpleLedger is a lightweight double-entry ledger REST API built with Java, Jersey (JAX-RS), MySQL, and Tomcat.

This project was developed for the **Assessment for the role _"Head of Engineering, Global Accounts"_**.

## Test Deployment

The project is already deployed for testing at:

**[http://165.73.244.67:8080/SimpleLedger](http://165.73.244.67:8080/SimpleLedger)**

API base URL on that deployment:

`http://165.73.244.67:8080/SimpleLedger/api`

---

## Reviewer Notes

GitHub issues were created to track the work items, and the implementation was delivered as a series of focused commits to guide reviewers clearly through the development cycle.

---

## Tech Stack

- Java 8
- Maven
- Jersey (JAX-RS)
- MySQL 8
- Tomcat 9

---

## Quick Setup (Local)

1. Create schema/tables:
   - Run `schema.sql`
2. Configure Tomcat DataSource:
   - JNDI name: `jdbc/SimpleLedgerDB`
3. Build:
   - `mvn clean package`
4. Deploy generated WAR to Tomcat `webapps`

For full instructions, see `setup.md`.

---

## API Documentation

### Common Notes

- Content type: `application/json; charset=UTF-8`
- Pagination defaults:
  - `offset=0`
  - `limit=20` (max `100`)
- Error format (typical):
  ```json
  {
    "status": 400,
    "error": "Validation message",
    "path": "/SimpleLedger/api/..."
  }
  ```

---

## 1) Create Account

### Endpoint
`POST /api/Accounts`

### Request Example
```json
{
  "name": "Cash",
  "normalBalance": "DEBIT"
}
```

### Success Response Example
```json
{
  "id": 1
}
```

### Validation Rules
- `name` is required and must be non-empty
- `normalBalance` is required and must be either `DEBIT` or `CREDIT`

### cURL Example
```bash
curl -X POST "http://165.73.244.67:8080/SimpleLedger/api/Accounts" \
  -H "Content-Type: application/json" \
  -d '{"name":"Cash","normalBalance":"DEBIT"}'
```

---

## 2) List Accounts

### Endpoint
`GET /api/Accounts?offset=0&limit=20`

### Request Example
No request body.

### Success Response Example
```json
{
  "offset": 0,
  "limit": 20,
  "accounts": [
    {
      "id": 1,
      "name": "Cash",
      "normalBalance": "DEBIT"
    },
    {
      "id": 2,
      "name": "Customer Deposits",
      "normalBalance": "CREDIT"
    }
  ]
}
```

### cURL Example
```bash
curl "http://165.73.244.67:8080/SimpleLedger/api/Accounts?offset=0&limit=20"
```

---

## 3) Get Account by ID

### Endpoint
`GET /api/Accounts/{accountId}`

### Request Example
No request body.  
Example path: `/api/Accounts/1`

### Success Response Example
```json
{
  "id": 1,
  "name": "Cash",
  "normalBalance": "DEBIT"
}
```

### Error Response Example (Not Found)
```json
{
  "status": 404,
  "error": "Account not found",
  "path": "/SimpleLedger/api/Accounts/9999"
}
```

### cURL Example
```bash
curl "http://165.73.244.67:8080/SimpleLedger/api/Accounts/1"
```

---

## 4) Get Account Journal Entries

### Endpoint
`GET /api/Accounts/{accountId}/JournalEntries?offset=0&limit=20`

### Request Example
No request body.  
Example path: `/api/Accounts/1/JournalEntries?offset=0&limit=20`

### Success Response Example
```json
{
  "accountId": 1,
  "offset": 0,
  "limit": 20,
  "journalEntries": [
    {
      "id": 10,
      "transactionId": 7,
      "type": "DEBIT",
      "amount": 100.00,
      "createdAt": "2026-04-24 19:20:11.0"
    }
  ]
}
```

### cURL Example
```bash
curl "http://165.73.244.67:8080/SimpleLedger/api/Accounts/1/JournalEntries?offset=0&limit=20"
```

---

## 5) Get Account Balance

### Endpoint
`GET /api/Accounts/{accountId}/balance`

### Request Example
No request body.  
Example path: `/api/Accounts/1/balance`

### Success Response Example
```json
{
  "accountId": 1,
  "balance": 250.00,
  "warning": null
}
```

### Possible Warning
`warning` may contain:
- `"Balance direction does not match account normalBalance (DEBIT)"`
- `"Balance direction does not match account normalBalance (CREDIT)"`

### cURL Example
```bash
curl "http://165.73.244.67:8080/SimpleLedger/api/Accounts/1/balance"
```

---

## 6) Create Transaction

### Endpoint
`POST /api/Transactions`

### Request Example
```json
{
  "externalId": "EXT-TRX-1001",
  "idempotencyId": "IDE-TRX-1001",
  "type": "DEPOSIT",
  "notes": "Initial customer deposit",
  "journalEntries": [
    {
      "accountId": 1,
      "type": "DEBIT",
      "amount": 100.00
    },
    {
      "accountId": 2,
      "type": "CREDIT",
      "amount": 100.00
    }
  ]
}
```

### Success Response Example
```json
{
  "id": 7
}
```

### Idempotency Behavior
If the same `idempotencyId` is submitted again, API returns the existing transaction id:
```json
{
  "id": 7
}
```

### Validation Rules
- `externalId`, `idempotencyId`, `type` are required
- `journalEntries` is required and must contain at least 2 entries
- Each entry must include:
  - positive `accountId`
  - `type` in (`DEBIT`, `CREDIT`)
  - `amount` > 0
- Total debit amount must equal total credit amount
- `type` must exist in transaction types (seed examples: `DEPOSIT`, `TRANSFER`, `CARD`)

### cURL Example
```bash
curl -X POST "http://165.73.244.67:8080/SimpleLedger/api/Transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "externalId":"EXT-TRX-1001",
    "idempotencyId":"IDE-TRX-1001",
    "type":"DEPOSIT",
    "notes":"Initial customer deposit",
    "journalEntries":[
      {"accountId":1,"type":"DEBIT","amount":100.00},
      {"accountId":2,"type":"CREDIT","amount":100.00}
    ]
  }'
```

---

## 7) List Transactions

### Endpoint
`GET /api/Transactions?offset=0&limit=20`

### Request Example
No request body.

### Success Response Example
```json
{
  "offset": 0,
  "limit": 20,
  "transactions": [
    {
      "id": 7,
      "idempotencyId": "IDE-TRX-1001",
      "type": "DEPOSIT",
      "externalId": "EXT-TRX-1001",
      "notes": "Initial customer deposit",
      "createdAt": "2026-04-24 19:20:11.0"
    }
  ]
}
```

### cURL Example
```bash
curl "http://165.73.244.67:8080/SimpleLedger/api/Transactions?offset=0&limit=20"
```

---

## 8) Get Transaction by ID

### Endpoint
`GET /api/Transactions/{transactionId}`

### Request Example
No request body.  
Example path: `/api/Transactions/7`

### Success Response Example
```json
{
  "id": 7,
  "idempotencyId": "IDE-TRX-1001",
  "type": "DEPOSIT",
  "externalId": "EXT-TRX-1001",
  "notes": "Initial customer deposit",
  "createdAt": "2026-04-24 19:20:11.0",
  "journalEntries": [
    {
      "accountId": 1,
      "accountName": "Cash",
      "type": "DEBIT",
      "amount": 100.00
    },
    {
      "accountId": 2,
      "accountName": "Customer Deposits",
      "type": "CREDIT",
      "amount": 100.00
    }
  ]
}
```

### Error Response Example (Not Found)
```json
{
  "status": 404,
  "error": "Transaction not found",
  "path": "/SimpleLedger/api/Transactions/99999"
}
```

### cURL Example
```bash
curl "http://165.73.244.67:8080/SimpleLedger/api/Transactions/7"
```

---

## Database Schema

Main tables:

- `sld_transaction_types`
- `sld_accounts`
- `sld_transactions`
- `sld_journal_entries`

Seeded transaction types:
- `DEPOSIT`
- `TRANSFER`
- `CARD`

See `schema.sql` for full DDL.

---

## Notes

- CORS is enabled with `Access-Control-Allow-Origin: *`.
- API is designed around double-entry accounting (balanced debit/credit entries).
- SQL and validation errors are returned as JSON using the common error structure.
