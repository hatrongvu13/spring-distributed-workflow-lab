# Production roadmap

1. Replace H2 with PostgreSQL and Flyway per service.
2. Implement Transactional Outbox in request API, orchestrator, worker and target; relay with Debezium or a polling publisher.
3. Replace local retry timer with RabbitMQ delayed exchange/TTL retry queues, Quartz, Temporal or Camunda.
4. Use publisher confirms, mandatory publishing, quorum queues and broker policies.
5. Add inbox tables with unique event IDs to every consumer.
6. Use JSON Schema/Avro/Protobuf and explicit schema versions.
7. Add OpenTelemetry trace/context propagation and metrics for age, deadline slack, attempts, DLQ size and compensation latency.
8. Secure with TLS, per-service credentials, vhosts, least-privilege RabbitMQ permissions and secret rotation.
9. Add partitioning strategy and ordering rules. Avoid assuming global event order.
10. Add chaos tests: broker outage, duplicate delivery, consumer crash after DB commit, delayed status, out-of-order rollback and replay.
11. Use Kubernetes readiness/liveness, graceful shutdown, PodDisruptionBudget and autoscaling based on queue depth plus oldest-message age.
12. Replace generic rollback with step-specific compensators and maintain a compensation journal.
