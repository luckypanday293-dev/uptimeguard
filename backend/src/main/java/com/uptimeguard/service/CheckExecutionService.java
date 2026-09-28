package com.uptimeguard.service;

import com.uptimeguard.dto.CheckResultResponse;
import com.uptimeguard.model.*;
import com.uptimeguard.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Service
public class CheckExecutionService {
    private final MonitorRepository monitorRepository;
    private final CheckResultRepository checkResultRepository;
    private final IncidentRepository incidentRepository;
    private final NotificationRepository notificationRepository;
    private final WebsiteChecker websiteChecker;
    private final MonitorMapper mapper;

    public CheckExecutionService(
            MonitorRepository monitorRepository,
            CheckResultRepository checkResultRepository,
            IncidentRepository incidentRepository,
            NotificationRepository notificationRepository,
            WebsiteChecker websiteChecker,
            MonitorMapper mapper) {
        this.monitorRepository = monitorRepository;
        this.checkResultRepository = checkResultRepository;
        this.incidentRepository = incidentRepository;
        this.notificationRepository = notificationRepository;
        this.websiteChecker = websiteChecker;
        this.mapper = mapper;
    }

    @Transactional
    public CheckResultResponse runCheck(Long monitorId) {
        Monitor monitor = monitorRepository.findById(monitorId)
                .orElseThrow(() -> new IllegalArgumentException("Monitor not found: " + monitorId));

        MonitorStatus previousStatus = monitor.getCurrentStatus();
        ProbeResult finalProbe = null;
        int attempts = 0;
        int maxAttempts = monitor.getRetryCount() + 1;

        while (attempts < maxAttempts) {
            attempts++;
            finalProbe = websiteChecker.check(monitor);
            if (finalProbe.successful()) {
                break;
            }
        }

        Instant now = Instant.now();
        boolean successful = finalProbe != null && finalProbe.successful();

        CheckResult result = new CheckResult();
        result.setMonitor(monitor);
        result.setCheckedAt(now);
        result.setSuccessful(successful);
        result.setStatusCode(finalProbe == null ? null : finalProbe.statusCode());
        result.setResponseTimeMs(finalProbe == null ? null : finalProbe.responseTimeMs());
        result.setAttempts(attempts);
        result.setErrorMessage(finalProbe == null ? "No check result" : finalProbe.errorMessage());
        result = checkResultRepository.save(result);

        monitor.setLastCheckedAt(now);
        monitor.setLastResponseTimeMs(result.getResponseTimeMs());
        monitor.setCurrentStatus(successful ? MonitorStatus.UP : MonitorStatus.DOWN);
        monitorRepository.save(monitor);

        if (!successful) {
            openIncidentIfNeeded(monitor, now, result.getErrorMessage());
        } else if (previousStatus == MonitorStatus.DOWN) {
            resolveIncidentIfPresent(monitor, now);
        }

        return mapper.toCheckResponse(result);
    }

    private void openIncidentIfNeeded(Monitor monitor, Instant now, String failureMessage) {
        if (incidentRepository.findFirstByMonitorIdAndStatusOrderByStartedAtDesc(monitor.getId(), IncidentStatus.OPEN).isPresent()) {
            return;
        }

        Incident incident = new Incident();
        incident.setMonitor(monitor);
        incident.setStatus(IncidentStatus.OPEN);
        incident.setStartedAt(now);
        incident.setFailureMessage(failureMessage == null ? "Health check failed" : failureMessage);
        incident = incidentRepository.save(incident);

        Notification notification = new Notification();
        notification.setMonitor(monitor);
        notification.setIncident(incident);
        notification.setType(NotificationType.INCIDENT_OPENED);
        notification.setMessage(monitor.getName() + " is DOWN. Incident #" + incident.getId() + " opened.");
        notification.setCreatedAt(now);
        notificationRepository.save(notification);
    }

    private void resolveIncidentIfPresent(Monitor monitor, Instant now) {
        incidentRepository.findFirstByMonitorIdAndStatusOrderByStartedAtDesc(monitor.getId(), IncidentStatus.OPEN)
                .ifPresent(incident -> {
                    incident.setStatus(IncidentStatus.RESOLVED);
                    incident.setRecoveredAt(now);
                    long seconds = Math.max(0, Duration.between(incident.getStartedAt(), now).toSeconds());
                    incident.setOutageDurationSeconds(seconds);
                    incidentRepository.save(incident);

                    Notification notification = new Notification();
                    notification.setMonitor(monitor);
                    notification.setIncident(incident);
                    notification.setType(NotificationType.RECOVERY);
                    notification.setMessage(monitor.getName() + " recovered after " + seconds + " seconds.");
                    notification.setCreatedAt(now);
                    notificationRepository.save(notification);
                });
    }
}
