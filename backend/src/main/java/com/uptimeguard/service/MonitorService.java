package com.uptimeguard.service;

import com.uptimeguard.dto.MonitorRequest;
import com.uptimeguard.dto.MonitorResponse;
import com.uptimeguard.model.Monitor;
import com.uptimeguard.model.MonitorStatus;
import com.uptimeguard.repository.MonitorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MonitorService {
    private final MonitorRepository monitorRepository;
    private final MonitorMapper mapper;

    public MonitorService(MonitorRepository monitorRepository, MonitorMapper mapper) {
        this.monitorRepository = monitorRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<MonitorResponse> findAll() {
        return monitorRepository.findAll().stream().map(mapper::toMonitorResponse).toList();
    }

    @Transactional(readOnly = true)
    public Monitor getEntity(Long id) {
        return monitorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Monitor not found: " + id));
    }

    @Transactional
    public MonitorResponse create(MonitorRequest request) {
        Monitor monitor = new Monitor();
        apply(monitor, request);
        monitor.setCurrentStatus(MonitorStatus.UNKNOWN);
        return mapper.toMonitorResponse(monitorRepository.save(monitor));
    }

    @Transactional
    public MonitorResponse update(Long id, MonitorRequest request) {
        Monitor monitor = getEntity(id);
        apply(monitor, request);
        return mapper.toMonitorResponse(monitorRepository.save(monitor));
    }

    @Transactional
    public void delete(Long id) {
        if (!monitorRepository.existsById(id)) {
            throw new IllegalArgumentException("Monitor not found: " + id);
        }
        monitorRepository.deleteById(id);
    }

    private void apply(Monitor monitor, MonitorRequest request) {
        monitor.setName(request.name().trim());
        monitor.setUrl(request.url().trim());
        monitor.setExpectedStatus(request.expectedStatus() == null ? 200 : request.expectedStatus());
        monitor.setTimeoutMs(request.timeoutMs() == null ? 3000 : request.timeoutMs());
        monitor.setRetryCount(request.retryCount() == null ? 2 : request.retryCount());
        monitor.setCheckIntervalSeconds(request.checkIntervalSeconds() == null ? 30 : request.checkIntervalSeconds());
        monitor.setActive(request.active() == null || request.active());
    }
}
