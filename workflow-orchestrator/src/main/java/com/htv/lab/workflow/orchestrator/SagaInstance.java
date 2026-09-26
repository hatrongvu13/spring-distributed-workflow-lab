package com.htv.lab.workflow.orchestrator;

import jakarta.persistence.*;

import java.time.*;

@Entity
public class SagaInstance {
    @Id
    private String requestId;
    private String correlationId;
    private String requestType;
    @Lob
    private String payloadJson;
    private Instant deadline;
    private int maxAttempts;
    private long retryDelayMs;
    private int attempt;
    private String state;
    private boolean terminal;

    protected SagaInstance() {
    }

    public SagaInstance(String r, String c, String t, String p, Instant d, int m, long delay) {
        requestId = r;
        correlationId = c;
        requestType = t;
        payloadJson = p;
        deadline = d;
        maxAttempts = m;
        retryDelayMs = delay;
        state = "RECEIVED";
    }

    public void dispatched(int a) {
        attempt = a;
        state = "DISPATCHED";
    }

    public void state(String s) {
        state = s;
    }

    public void terminal(String s) {
        state = s;
        terminal = true;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getRequestType() {
        return requestType;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public Instant getDeadline() {
        return deadline;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public long getRetryDelayMs() {
        return retryDelayMs;
    }

    public int getAttempt() {
        return attempt;
    }

    public String getState() {
        return state;
    }

    public boolean isTerminal() {
        return terminal;
    }
}