package com.htv.lab.workflow.api;

import jakarta.persistence.*;

import java.time.*;

@Entity
@Table(name = "workflow_requests")
public class RequestRecord {
    @Id
    private String id;
    private String correlationId;
    private String requestType;
    @Enumerated(EnumType.STRING)
    private com.htv.lab.workflow.contracts.WorkflowMessages.Status status;
    private Instant deadline;
    private int attempt;
    private String lastError;
    private Instant updatedAt;

    protected RequestRecord() {
    }

    public RequestRecord(String i, String c, String t, Instant d) {
        id = i;
        correlationId = c;
        requestType = t;
        deadline = d;
        status = com.htv.lab.workflow.contracts.WorkflowMessages.Status.RECEIVED;
        updatedAt = Instant.now();
    }

    public void update(com.htv.lab.workflow.contracts.WorkflowMessages.Status s, int a, String e) {
        status = s;
        attempt = a;
        lastError = e;
        updatedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getRequestType() {
        return requestType;
    }

    public com.htv.lab.workflow.contracts.WorkflowMessages.Status getStatus() {
        return status;
    }

    public Instant getDeadline() {
        return deadline;
    }

    public int getAttempt() {
        return attempt;
    }

    public String getLastError() {
        return lastError;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}