# Architecture

## Recommended name
**Distributed Deadline Saga Lab** or **Spring Distributed Workflow Lab**.

## Patterns
- Orchestrated Saga and Process Manager for cross-service state.
- State Machine for lifecycle decisions.
- Command/Event separation: commands request work; status events report facts.
- Idempotent Consumer keyed by `requestId`.
- Compensating Transaction instead of database rollback across services.
- Correlation ID on every command/event.
- Deadline propagation in every command.
- Retry policy owned by orchestrator.
- Dead Letter Queue for poison messages.
- CQRS-lite: request API keeps a status read model from events.

## Why compensation, not distributed ACID
Each service owns its database. Once a service commits, another service cannot safely roll it back through a shared transaction. A rollback message therefore invokes a domain-specific compensation such as releasing inventory, cancelling a reservation or reversing a provisional entry.

## Message contract fields
`requestId`, `correlationId`, `eventId`, `service`, `step`, `status`, `attempt`, `deadline`, `errorCode`, `message`, `occurredAt`. Production should also add `schemaVersion`, `causationId`, `traceparent`, tenant, content type and producer version.
