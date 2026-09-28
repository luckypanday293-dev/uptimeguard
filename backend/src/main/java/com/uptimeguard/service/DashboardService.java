package com.uptimeguard.service;

import com.uptimeguard.dto.*;
import com.uptimeguard.model.IncidentStatus;
import com.uptimeguard.model.MonitorStatus;
import com.uptimeguard.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DashboardService {
    private final MonitorRepository monitorRepository;
    private final IncidentRepository incidentRepository;
    private final NotificationRepository notificationRepository;
    private final MonitorMapper mapper;

    public DashboardService(MonitorRepository monitorRepository,
                            IncidentRepository incidentRepository,
                            NotificationRepository notificationRepository,
                            MonitorMapper mapper) {
        this.monitorRepository = monitorRepository;
        this.incidentRepository = incidentRepository;
        this.notificationRepository = notificationRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public DashboardResponse dashboard() {
        var monitors = monitorRepository.findAll();
        long up = monitors.stream().filter(m -> m.getCurrentStatus() == MonitorStatus.UP).count();
        long down = monitors.stream().filter(m -> m.getCurrentStatus() == MonitorStatus.DOWN).count();
        double avg = monitors.stream()
                .filter(m -> m.getLastResponseTimeMs() != null)
                .mapToLong(m -> m.getLastResponseTimeMs())
                .average().orElse(0.0);

        List<MonitorResponse> monitorResponses = monitors.stream().map(mapper::toMonitorResponse).toList();
        List<IncidentResponse> incidents = incidentRepository.findAllByOrderByStartedAtDesc().stream()
                .limit(8).map(mapper::toIncidentResponse).toList();
        List<NotificationResponse> notifications = notificationRepository
                .findAllByOrderByCreatedAtDesc(PageRequest.of(0, 8)).stream()
                .map(mapper::toNotificationResponse).toList();

        return new DashboardResponse(
                monitors.size(), up, down,
                incidentRepository.countByStatus(IncidentStatus.OPEN),
                notificationRepository.countByReadFlagFalse(),
                Math.round(avg * 10.0) / 10.0,
                monitorResponses, incidents, notifications
        );
    }
}
