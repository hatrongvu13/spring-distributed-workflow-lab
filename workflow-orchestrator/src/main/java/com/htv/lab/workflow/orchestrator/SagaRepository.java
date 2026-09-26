package com.htv.lab.workflow.orchestrator;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SagaRepository extends JpaRepository<SagaInstance, String> {
    List<SagaInstance> findByTerminalFalse();
}