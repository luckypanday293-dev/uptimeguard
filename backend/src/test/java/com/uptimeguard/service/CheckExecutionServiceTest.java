package com.uptimeguard.service;

import com.uptimeguard.dto.CheckResultResponse;
import com.uptimeguard.model.*;
import com.uptimeguard.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CheckExecutionServiceTest {
    private MonitorRepository monitorRepository;
    private CheckResultRepository checkResultRepository;
    private IncidentRepository incidentRepository;
    private NotificationRepository notificationRepository;
    private WebsiteChecker websiteChecker;
    private MonitorMapper mapper;
    private CheckExecutionService service;

    @BeforeEach
    void setUp() {
        monitorRepository = mock(MonitorRepository.class);
        checkResultRepository = mock(CheckResultRepository.class);
        incidentRepository = mock(IncidentRepository.class);
        notificationRepository = mock(NotificationRepository.class);
        websiteChecker = mock(WebsiteChecker.class);
        mapper = mock(MonitorMapper.class);
        service = new CheckExecutionService(monitorRepository, checkResultRepository, incidentRepository,
                notificationRepository, websiteChecker, mapper);
    }

    @Test
    void retriesFailedChecksAndOpensIncident() {
        Monitor monitor = monitor(1L, MonitorStatus.UP, 1);
        when(monitorRepository.findById(1L)).thenReturn(Optional.of(monitor));
        when(websiteChecker.check(monitor))
                .thenReturn(new ProbeResult(false, 503, 120, "Expected HTTP 200 but received 503"));
        when(checkResultRepository.save(any(CheckResult.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(incidentRepository.findFirstByMonitorIdAndStatusOrderByStartedAtDesc(1L, IncidentStatus.OPEN))
                .thenReturn(Optional.empty());
        when(incidentRepository.save(any(Incident.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toCheckResponse(any(CheckResult.class)))
                .thenReturn(new CheckResultResponse(null, 1L, "Example", Instant.now(), false, 503, 120L, 2, "failed"));

        service.runCheck(1L);

        verify(websiteChecker, times(2)).check(monitor);
        ArgumentCaptor<CheckResult> resultCaptor = ArgumentCaptor.forClass(CheckResult.class);
        verify(checkResultRepository).save(resultCaptor.capture());
        assertEquals(2, resultCaptor.getValue().getAttempts());
        assertFalse(resultCaptor.getValue().getSuccessful());
        assertEquals(MonitorStatus.DOWN, monitor.getCurrentStatus());
        verify(incidentRepository).save(any(Incident.class));
        verify(notificationRepository).save(argThat(n -> n.getType() == NotificationType.INCIDENT_OPENED));
    }

    @Test
    void successfulCheckRecoversOpenIncident() {
        Monitor monitor = monitor(2L, MonitorStatus.DOWN, 2);
        Incident incident = new Incident();
        incident.setMonitor(monitor);
        incident.setStatus(IncidentStatus.OPEN);
        incident.setStartedAt(Instant.now().minusSeconds(45));

        when(monitorRepository.findById(2L)).thenReturn(Optional.of(monitor));
        when(websiteChecker.check(monitor)).thenReturn(new ProbeResult(true, 200, 42, null));
        when(checkResultRepository.save(any(CheckResult.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(incidentRepository.findFirstByMonitorIdAndStatusOrderByStartedAtDesc(2L, IncidentStatus.OPEN))
                .thenReturn(Optional.of(incident));
        when(incidentRepository.save(any(Incident.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toCheckResponse(any(CheckResult.class)))
                .thenReturn(new CheckResultResponse(null, 2L, "Example", Instant.now(), true, 200, 42L, 1, null));

        service.runCheck(2L);

        verify(websiteChecker, times(1)).check(monitor);
        assertEquals(MonitorStatus.UP, monitor.getCurrentStatus());
        assertEquals(IncidentStatus.RESOLVED, incident.getStatus());
        assertNotNull(incident.getRecoveredAt());
        assertNotNull(incident.getOutageDurationSeconds());
        verify(notificationRepository).save(argThat(n -> n.getType() == NotificationType.RECOVERY));
    }

    private Monitor monitor(Long id, MonitorStatus status, int retries) {
        Monitor monitor = new Monitor();
        monitor.setId(id);
        monitor.setName("Example");
        monitor.setUrl("https://example.com");
        monitor.setExpectedStatus(200);
        monitor.setTimeoutMs(1000);
        monitor.setRetryCount(retries);
        monitor.setCheckIntervalSeconds(30);
        monitor.setActive(true);
        monitor.setCurrentStatus(status);
        return monitor;
    }
}
