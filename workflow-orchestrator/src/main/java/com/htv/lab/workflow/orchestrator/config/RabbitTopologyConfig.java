package com.htv.lab.workflow.orchestrator.config;

import com.htv.lab.workflow.contracts.Topology;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.*;

@Configuration
public class RabbitTopologyConfig {
    @Bean
    TopicExchange workflowExchange() {
        return ExchangeBuilder.topicExchange(Topology.EXCHANGE).durable(true).build();
    }

    @Bean
    DirectExchange workflowDlx() {
        return ExchangeBuilder.directExchange(Topology.DLX).durable(true).build();
    }

    @Bean
    Queue requestQueue() {
        return q(Topology.REQUEST_Q);
    }

    @Bean
    Queue workQueue() {
        return q(Topology.WORK_Q);
    }

    @Bean
    Queue targetQueue() {
        return q(Topology.TARGET_Q);
    }

    @Bean
    Queue statusQueue() {
        return q(Topology.STATUS_Q);
    }

    @Bean
    Queue apiStatusQueue() {
        return q(Topology.API_STATUS_Q);
    }

    @Bean
    Queue rollbackQueue() {
        return q(Topology.ROLLBACK_Q);
    }

    @Bean
    Queue rollbackExecuteQueue() {
        return q(Topology.ROLLBACK_EXECUTE_Q);
    }

    @Bean
    Queue deadQueue() {
        return QueueBuilder.durable(Topology.DLQ).build();
    }

    @Bean
    Declarables bindings(TopicExchange e, DirectExchange d) {
        return new Declarables(BindingBuilder.bind(requestQueue()).to(e).with("request.received"), BindingBuilder.bind(workQueue()).to(e).with("work.command"), BindingBuilder.bind(targetQueue()).to(e).with("target.command"), BindingBuilder.bind(statusQueue()).to(e).with("status.#"), BindingBuilder.bind(apiStatusQueue()).to(e).with("status.#"), BindingBuilder.bind(rollbackQueue()).to(e).with("rollback.requested"), BindingBuilder.bind(rollbackExecuteQueue()).to(e).with("rollback.execute"), BindingBuilder.bind(deadQueue()).to(d).with("dead"));
    }

    private Queue q(String n) {
        return QueueBuilder.durable(n).deadLetterExchange(Topology.DLX).deadLetterRoutingKey("dead").build();
    }
}
