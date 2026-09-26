# Spring Distributed Workflow Lab

Demo monorepo cho workflow phân tán có HTTP ingress, deadline, retry, status event, compensation/rollback và terminal completion qua RabbitMQ. Mô hình chính là **orchestrated saga** kết hợp **process manager**, **idempotent consumer**, **transactional state machine** và **DLQ**.

## Modules
- `request-api`: nhận request mặc định, trả `202 Accepted`, cung cấp status polling và rollback API.
- `workflow-orchestrator`: lưu saga state, dispatch công việc, quyết định retry/deadline/rollback và hoàn tất workflow.
- `worker-service`: xử lý nghiệp vụ có idempotency và compensation.
- `target-service`: service đích, phát `ALL_SERVICES_DONE`.
- `workflow-contracts`: message contracts và topology names.

## Run
```bash
docker compose up -d
mvn clean package
mvn -pl request-api spring-boot:run
mvn -pl workflow-orchestrator spring-boot:run
mvn -pl worker-service spring-boot:run
mvn -pl target-service spring-boot:run
```
RabbitMQ UI: `http://localhost:15672`, guest/guest.

## Default request
```bash
curl -X POST http://localhost:8080/api/v1/requests -H 'Content-Type: application/json' -d '{"requestType":"DEFAULT","payload":{"customerId":"C001"},"deadlineSeconds":30,"maxAttempts":3,"retryDelayMs":2000}'
```
Poll returned request ID:
```bash
curl http://localhost:8080/api/v1/requests/REQUEST_ID
```

## Retry demo
The worker fails attempts 1 and 2 when payload contains `"fail":"true"`, then succeeds on attempt 3:
```bash
curl -X POST http://localhost:8080/api/v1/requests -H 'Content-Type: application/json' -d '{"requestType":"DEFAULT","payload":{"fail":"true"},"deadlineSeconds":30,"maxAttempts":3,"retryDelayMs":1000}'
```

## Rollback demo
```bash
curl -X POST 'http://localhost:8080/api/v1/requests/REQUEST_ID/rollback?reason=operator-cancelled'
```

## States
`RECEIVED -> DISPATCHED -> STEP_DONE -> TARGET_DISPATCHED -> ALL_SERVICES_DONE`.
Failure path: `FAILED -> RETRY_SCHEDULED -> DISPATCHED`, then `FAILED` when attempts are exhausted.
Deadline path: `TIMED_OUT -> rollback.execute -> ROLLED_BACK`.
Manual path: `ROLLBACK_REQUESTED -> rollback.execute -> ROLLED_BACK`.

## Important demo limitations
The in-memory retry timer is intentionally simple. A production orchestrator must use durable delayed messages, a scheduler table with locking, Quartz, or Temporal/Camunda. Database updates and RabbitMQ publish are not atomic in this demo; add Transactional Outbox before production. H2 is local-only; use PostgreSQL plus Flyway. See `docs/production-roadmap.md`.
