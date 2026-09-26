package com.htv.lab.workflow.orchestrator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OrchestratorApplication {
    public static void main(String[] a) {
        SpringApplication.run(OrchestratorApplication.class, a);
    }
}