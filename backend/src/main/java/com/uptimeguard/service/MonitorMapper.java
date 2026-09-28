package com.uptimeguard.service;

import com.uptimeguard.dto.*;
import com.uptimeguard.model.*;
import com.uptimeguard.repository.CheckResultRepository;
import org.springframework.stereotype.Component;

@Component
public class MonitorMapper {
    private final CheckResultRepository checkResultRepository;

    public MonitorMapper(CheckResultRepository checkResultRepository) {
        this.checkResultRepository = checkResultRepository;
    }

    public MonitorResponse toMonitorResponse(Monitor monitor) {
        long total = checkResultRepository.countByMonitorId(monitor.getId());
        long successful = checkResultRepository.countByMonitorIdAndSuccessfulTrue(monitor.getId());
        double uptime = total == 0 ? 0.0 : Math.round((successful * 10000.0 / total)) / 100.0;
        return new MonitorResponse(
                monitor.getId(), monitor.getName(), monitor.getUrl(), monitor.getExpectedStatus(),
                monitor.getTimeoutMs(), monitor.getRetryCount(), monitor.getCheckIntervalSeconds(),
                monitor.getActive(), monitor.getCurrentStatus(), monitor.getLastCheckedAt(),
                monitor.getLastResponseTimeMs(), uptime
        );
    }

    public CheckResultResponse toCheckResponse(CheckResult result) {
        return new CheckResultResponse(result.getId(), result.getMonitor().getId(), result.getMonitor().getName(),
                result.getCheckedAt(), result.getSuccessful(), result.getStatusCode(), result.getResponseTimeMs(),
                result.getAttempts(), result.getErrorMessage());
    }

    public IncidentResponse toIncidentResponse(Incident incident) {
        return new IncidentResponse(incident.getId(), incident.getMonitor().getId(), incident.getMonitor().getName(),
                incident.getStatus(), incident.getStartedAt(), incident.getRecoveredAt(),
                incident.getOutageDurationSeconds(), incident.getFailureMessage());
    }

    public NotificationResponse toNotificationResponse(Notification notification) {
        Long incidentId = notification.getIncident() == null ? null : notification.getIncident().getId();
        return new NotificationResponse(notification.getId(), notification.getMonitor().getId(), notification.getMonitor().getName(),
                incidentId, notification.getType(), notification.getMessage(), notification.getCreatedAt(), notification.getReadFlag());
    }
}
