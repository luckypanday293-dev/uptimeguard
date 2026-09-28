package com.uptimeguard.config;

import com.uptimeguard.model.Monitor;
import com.uptimeguard.model.MonitorStatus;
import com.uptimeguard.repository.MonitorRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {
    private final MonitorRepository monitorRepository;

    public DataSeeder(MonitorRepository monitorRepository) {
        this.monitorRepository = monitorRepository;
    }

    @Override
    public void run(String... args) {
        if (monitorRepository.count() > 0) {
            return;
        }
        Monitor monitor = new Monitor();
        monitor.setName("UptimeGuard API");
        monitor.setUrl("http://localhost:8081/api/health");
        monitor.setExpectedStatus(200);
        monitor.setTimeoutMs(2000);
        monitor.setRetryCount(1);
        monitor.setCheckIntervalSeconds(20);
        monitor.setActive(true);
        monitor.setCurrentStatus(MonitorStatus.UNKNOWN);
        monitorRepository.save(monitor);
    }
}
