package com.htv.lab.workflow.contracts;

public final class Topology {
    private Topology() {
    }

    public static final String EXCHANGE = "workflow.events";
    public static final String REQUEST_Q = "workflow.request.received.q";
    public static final String WORK_Q = "workflow.work.q";
    public static final String TARGET_Q = "workflow.target.q";
    public static final String STATUS_Q = "workflow.status.orchestrator.q";
    public static final String API_STATUS_Q = "workflow.status.api.q";
    public static final String ROLLBACK_Q = "workflow.rollback.requested.q";
    public static final String ROLLBACK_EXECUTE_Q = "workflow.rollback.execute.q";
    public static final String DLX = "workflow.dlx";
    public static final String DLQ = "workflow.dead.q";
}