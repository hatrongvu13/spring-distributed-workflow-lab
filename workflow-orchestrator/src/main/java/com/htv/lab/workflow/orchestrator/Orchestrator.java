package com.htv.lab.workflow.orchestrator;

import com.htv.lab.workflow.contracts.*;
import com.htv.lab.workflow.contracts.WorkflowMessages.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;

@Component
public class Orchestrator {
    private final SagaRepository repo;
    private final RabbitTemplate rabbit;
    private final ScheduledExecutorService timer = Executors.newScheduledThreadPool(2);

    public Orchestrator(SagaRepository r, RabbitTemplate q) {
        repo = r;
        rabbit = q;
    }

    @RabbitListener(queues = Topology.REQUEST_Q)
    @Transactional
    public void accepted(String body) {
        var e = Json.read(body, RequestAccepted.class);
        if (repo.existsById(e.requestId())) return;
        var s = repo.save(new SagaInstance(e.requestId(), e.correlationId(), e.requestType(), Json.write(e.payload()), e.deadline(), e.maxAttempts(), e.retryDelayMs()));
        dispatch(s, 1);
    }

    @RabbitListener(queues = Topology.STATUS_Q)
    @Transactional
    public void status(String body) {
        var e = Json.read(body, StatusEvent.class);
        var s = repo.findById(e.requestId()).orElse(null);
        if (s == null || s.isTerminal()) return;
        if (e.status() == Status.STEP_DONE) {
            s.state("TARGET_DISPATCHED");
            rabbit.convertAndSend(Topology.EXCHANGE, "target.command", Json.write(new TargetCommand(s.getRequestId(), s.getCorrelationId(), Json.read(s.getPayloadJson(), Map.class), s.getDeadline())));
            emit(s, Status.TARGET_DISPATCHED, "target dispatched", null);
        } else if (e.status() == Status.FAILED) {
            if (Instant.now().isBefore(s.getDeadline()) && s.getAttempt() < s.getMaxAttempts()) {
                s.state("RETRY_SCHEDULED");
                emit(s, Status.RETRY_SCHEDULED, "retry scheduled", e.errorCode());
                timer.schedule(() -> retry(s.getRequestId()), s.getRetryDelayMs(), TimeUnit.MILLISECONDS);
            } else {
                s.terminal("FAILED");
                emit(s, Status.FAILED, "attempts exhausted", e.errorCode());
            }
        } else if (e.status() == Status.ALL_SERVICES_DONE) {
            s.terminal("ALL_SERVICES_DONE");
            emit(s, Status.ALL_SERVICES_DONE, "workflow completed", null);
        } else if (e.status() == Status.ROLLED_BACK) {
            s.terminal("ROLLED_BACK");
            emit(s, Status.ROLLED_BACK, "workflow compensated", null);
        }
    }

    @RabbitListener(queues = Topology.ROLLBACK_Q)
    @Transactional
    public void rollback(String body) {
        var c = Json.read(body, RollbackCommand.class);
        repo.findById(c.requestId()).ifPresent(s -> {
            if (!s.isTerminal()) {
                s.state("ROLLBACK_REQUESTED");
                emit(s, Status.ROLLBACK_REQUESTED, c.reason(), null);
                rabbit.convertAndSend(Topology.EXCHANGE, "rollback.execute", body);
            }
        });
    }

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void deadlines() {
        repo.findByTerminalFalse().stream().filter(s -> Instant.now().isAfter(s.getDeadline())).forEach(s -> {
            s.terminal("TIMED_OUT");
            emit(s, Status.TIMED_OUT, "deadline exceeded", "DEADLINE_EXCEEDED");
            rabbit.convertAndSend(Topology.EXCHANGE, "rollback.execute", Json.write(new RollbackCommand(s.getRequestId(), s.getCorrelationId(), "deadline exceeded", Instant.now())));
        });
    }

    @Transactional
    public void retry(String id) {
        repo.findById(id).filter(s -> !s.isTerminal() && Instant.now().isBefore(s.getDeadline())).ifPresent(s -> dispatch(s, s.getAttempt() + 1));
    }

    private void dispatch(SagaInstance s, int attempt) {
        s.dispatched(attempt);
        repo.save(s);
        rabbit.convertAndSend(Topology.EXCHANGE, "work.command", Json.write(new WorkCommand(s.getRequestId(), s.getCorrelationId(), "PROCESS", s.getRequestType(), Json.read(s.getPayloadJson(), Map.class), attempt, s.getDeadline())));
        emit(s, Status.DISPATCHED, "work dispatched", null);
    }

    private void emit(SagaInstance s, Status st, String msg, String code) {
        rabbit.convertAndSend(Topology.EXCHANGE, "status." + st.name().toLowerCase(), Json.write(new StatusEvent(UUID.randomUUID().toString(), s.getRequestId(), s.getCorrelationId(), "orchestrator", "ORCHESTRATE", st, s.getAttempt(), code, msg, Instant.now(), s.getDeadline())));
    }
}
