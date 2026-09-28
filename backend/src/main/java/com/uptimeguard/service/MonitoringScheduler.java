package com.uptimeguard.service;

import com.uptimeguard.model.Monitor;
import com.uptimeguard.repository.MonitorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class MonitoringScheduler {
    private static final Logger log = LoggerFactory.getLogger(MonitoringScheduler.class);

    private final MonitorRepository monitorRepository;
    private final CheckExecutionService checkExecutionService;

    public MonitoringScheduler(MonitorRepository monitorRepository, CheckExecutionService checkExecutionService) {
        this.monitorRepository = monitorRepository;
        this.checkExecutionService = checkExecutionService;
    }

    @Scheduled(fixedDelayString = "${app.monitor.scheduler-delay-ms:15000}")
    public void runScheduledChecks() {
        for (Monitor monitor : monitorRepository.findByActiveTrueOrderByNameAsc()) {
            if (!isDue(monitor)) {
                continue;
            }
            try {
                checkExecutionService.runCheck(monitor.getId());
            } catch (Exception exception) {
                log.warn("Scheduled check failed for monitor {}: {}", monitor.getId(), exception.getMessage());
            }
        }
    }

    private boolean isDue(Monitor monitor) {
        if (monitor.getLastCheckedAt() == null) {
            return true;
        }
        Instant nextCheck = monitor.getLastCheckedAt().plus(monitor.getCheckIntervalSeconds(), ChronoUnit.SECONDS);
        return !nextCheck.isAfter(Instant.now());
    }
}
