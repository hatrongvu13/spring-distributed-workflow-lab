package com.htv.lab.workflow.contracts;

import java.time.Instant;
import java.util.Map;

public final class WorkflowMessages {
    private WorkflowMessages() {
    }

    public enum Status {RECEIVED, DISPATCHED, PROCESSING, RETRY_SCHEDULED, STEP_DONE, TARGET_DISPATCHED, ALL_SERVICES_DONE, ROLLBACK_REQUESTED, ROLLING_BACK, ROLLED_BACK, FAILED, TIMED_OUT}

    public record RequestAccepted(String requestId, String correlationId, String requestType,
                                  Map<String, String> payload, Instant acceptedAt, Instant deadline, int maxAttempts,
                                  long retryDelayMs) {
    }

    public record WorkCommand(String requestId, String correlationId, String step, String requestType,
                              Map<String, String> payload, int attempt, Instant deadline) {
    }

    public record TargetCommand(String requestId, String correlationId, Map<String, String> payload, Instant deadline) {
    }

    public record RollbackCommand(String requestId, String correlationId, String reason, Instant requestedAt) {
    }

    public record StatusEvent(String eventId, String requestId, String correlationId, String service, String step,
                              Status status, int attempt, String errorCode, String message, Instant occurredAt,
                              Instant deadline) {
    }
}
