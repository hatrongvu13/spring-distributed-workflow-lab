package com.htv.lab.workflow.worker;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
public class ProcessedWork {
    @Id
    private String requestId;
    private String result;
    private boolean compensated;
    private Instant updatedAt;

    protected ProcessedWork() {
    }

    public ProcessedWork(String id, String r) {
        requestId = id;
        result = r;
        updatedAt = Instant.now();
    }

    public void compensate() {
        compensated = true;
        updatedAt = Instant.now();
    }
}