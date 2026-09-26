package com.htv.lab.workflow.worker;

import com.htv.lab.workflow.contracts.*;
import com.htv.lab.workflow.contracts.WorkflowMessages.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Component
public class WorkerListener {
    private final ProcessedWorkRepository repo;
    private final RabbitTemplate rabbit;

    public WorkerListener(ProcessedWorkRepository r, RabbitTemplate q) {
        repo = r;
        rabbit = q;
    }

    @RabbitListener(queues = Topology.WORK_Q)
    @Transactional
    public void work(String body) {
        var c = Json.read(body, WorkCommand.class);
        if (Instant.now().isAfter(c.deadline())) {
            emit(c, Status.FAILED, "DEADLINE_EXCEEDED", "command expired");
            return;
        }
        if (repo.existsById(c.requestId())) {
            emit(c, Status.STEP_DONE, null, "idempotent replay");
            return;
        }
        try {
            if ("true".equalsIgnoreCase(c.payload().get("fail")) && c.attempt() < 3)
                throw new IllegalStateException("simulated transient failure");
            repo.save(new ProcessedWork(c.requestId(), "processed:" + c.requestType()));
            emit(c, Status.STEP_DONE, null, "worker completed");
        } catch (Exception e) {
            emit(c, Status.FAILED, "WORKER_ERROR", e.getMessage());
        }
    }

    @RabbitListener(queues = Topology.ROLLBACK_EXECUTE_Q)
    @Transactional
    public void rollback(String body) {
        var c = Json.read(body, RollbackCommand.class);
        repo.findById(c.requestId()).ifPresent(x -> {
            x.compensate();
            repo.save(x);
        });
        rabbit.convertAndSend(Topology.EXCHANGE, "status.rolled_back", Json.write(new StatusEvent(UUID.randomUUID().toString(), c.requestId(), c.correlationId(), "worker-service", "PROCESS", Status.ROLLED_BACK, 0, null, "compensation completed", Instant.now(), null)));
    }

    private void emit(WorkCommand c, Status s, String code, String msg) {
        rabbit.convertAndSend(Topology.EXCHANGE, "status." + s.name().toLowerCase(), Json.write(new StatusEvent(UUID.randomUUID().toString(), c.requestId(), c.correlationId(), "worker-service", c.step(), s, c.attempt(), code, msg, Instant.now(), c.deadline())));
    }
}
