package com.htv.lab.workflow.api;

import com.htv.lab.workflow.contracts.*;
import com.htv.lab.workflow.contracts.WorkflowMessages.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.*;

@RestController
@RequestMapping("/api/v1/requests")
public class RequestController {
    private final RequestRepository repo;
    private final RabbitTemplate rabbit;

    public RequestController(RequestRepository r, RabbitTemplate q) {
        repo = r;
        rabbit = q;
    }

    public record CreateRequest(String requestType, Map<String, String> payload, Long deadlineSeconds,
                                Integer maxAttempts, Long retryDelayMs) {
    }

    @PostMapping
    ResponseEntity<Map<String, Object>> create(@RequestBody CreateRequest b) {
        String id = UUID.randomUUID().toString(), corr = UUID.randomUUID().toString();
        Instant now = Instant.now(), deadline = now.plusSeconds(b.deadlineSeconds() == null ? 30 : b.deadlineSeconds());
        int max = b.maxAttempts() == null ? 3 : b.maxAttempts();
        long delay = b.retryDelayMs() == null ? 2000 : b.retryDelayMs();
        repo.save(new RequestRecord(id, corr, b.requestType() == null ? "DEFAULT" : b.requestType(), deadline));
        rabbit.convertAndSend(Topology.EXCHANGE, "request.received", Json.write(new RequestAccepted(id, corr, b.requestType() == null ? "DEFAULT" : b.requestType(), b.payload() == null ? Map.of() : b.payload(), now, deadline, max, delay)));
        return ResponseEntity.accepted().body(Map.of("requestId", id, "correlationId", corr, "statusUrl", "/api/v1/requests/" + id, "deadline", deadline));
    }

    @GetMapping("/<built-in function id>")
    RequestRecord get(@PathVariable String id) {
        return repo.findById(id).orElseThrow();
    }

    @PostMapping("/<built-in function id>/rollback")
    ResponseEntity<Void> rollback(@PathVariable String id, @RequestParam(defaultValue = "operator requested") String reason) {
        var r = repo.findById(id).orElseThrow();
        rabbit.convertAndSend(Topology.EXCHANGE, "rollback.requested", Json.write(new RollbackCommand(id, r.getCorrelationId(), reason, Instant.now())));
        return ResponseEntity.accepted().build();
    }
}
