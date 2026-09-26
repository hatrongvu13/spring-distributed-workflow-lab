package com.htv.lab.workflow.api;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestRepository extends JpaRepository<RequestRecord, String> {
}