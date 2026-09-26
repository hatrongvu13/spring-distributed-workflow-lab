package com.htv.lab.workflow.api;

import com.htv.lab.workflow.contracts.*;
import com.htv.lab.workflow.contracts.WorkflowMessages.StatusEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class StatusListener {
    private final RequestRepository repo;

    public StatusListener(RequestRepository r) {
        repo = r;
    }

    @RabbitListener(queues = Topology.API_STATUS_Q)
    public void status(String body) {
        StatusEvent e = Json.read(body, StatusEvent.class);
        repo.findById(e.requestId()).ifPresent(r -> {
            r.update(e.status(), e.attempt(), e.message());
            repo.save(r);
        });
    }
}