package com.uptimeguard.service;

import com.uptimeguard.dto.*;
import com.uptimeguard.model.IncidentStatus;
import com.uptimeguard.model.Notification;
import com.uptimeguard.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HistoryService {
    private final CheckResultRepository checkResultRepository;
    private final IncidentRepository incidentRepository;
    private final NotificationRepository notificationRepository;
    private final MonitorMapper mapper;

    public HistoryService(CheckResultRepository checkResultRepository,
                          IncidentRepository incidentRepository,
                          NotificationRepository notificationRepository,
                          MonitorMapper mapper) {
        this.checkResultRepository = checkResultRepository;
        this.incidentRepository = incidentRepository;
        this.notificationRepository = notificationRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<CheckResultResponse> checks(Long monitorId, int limit) {
        var pageable = PageRequest.of(0, Math.min(Math.max(limit, 1), 200));
        var results = monitorId == null
                ? checkResultRepository.findAllByOrderByCheckedAtDesc(pageable)
                : checkResultRepository.findByMonitorIdOrderByCheckedAtDesc(monitorId, pageable);
        return results.stream().map(mapper::toCheckResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<IncidentResponse> incidents(IncidentStatus status) {
        var incidents = status == null
                ? incidentRepository.findAllByOrderByStartedAtDesc()
                : incidentRepository.findByStatusOrderByStartedAtDesc(status);
        return incidents.stream().map(mapper::toIncidentResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> notifications(int limit) {
        return notificationRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, Math.min(Math.max(limit, 1), 100)))
                .stream().map(mapper::toNotificationResponse).toList();
    }

    @Transactional
    public NotificationResponse markNotificationRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + id));
        notification.setReadFlag(true);
        return mapper.toNotificationResponse(notificationRepository.save(notification));
    }
}
