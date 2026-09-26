package com.htv.lab.workflow.worker;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedWorkRepository extends JpaRepository<ProcessedWork, String> {
}