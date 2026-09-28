package com.uptimeguard.service;

import com.uptimeguard.dto.MonitorRequest;
import com.uptimeguard.dto.MonitorResponse;
import com.uptimeguard.model.Monitor;
import com.uptimeguard.repository.MonitorRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MonitorServiceTest {
    @Test
    void createAppliesDefaults() {
        MonitorRepository repository = mock(MonitorRepository.class);
        MonitorMapper mapper = mock(MonitorMapper.class);
        MonitorService service = new MonitorService(repository, mapper);

        when(repository.save(any(Monitor.class))).thenAnswer(invocation -> {
            Monitor monitor = invocation.getArgument(0);
            monitor.setId(10L);
            return monitor;
        });
        when(mapper.toMonitorResponse(any(Monitor.class))).thenAnswer(invocation -> {
            Monitor m = invocation.getArgument(0);
            return new MonitorResponse(m.getId(), m.getName(), m.getUrl(), m.getExpectedStatus(), m.getTimeoutMs(),
                    m.getRetryCount(), m.getCheckIntervalSeconds(), m.getActive(), m.getCurrentStatus(),
                    m.getLastCheckedAt(), m.getLastResponseTimeMs(), 0.0);
        });

        MonitorResponse response = service.create(new MonitorRequest("Docs", "https://example.com", null, null, null, null, null));

        assertEquals(200, response.expectedStatus());
        assertEquals(3000, response.timeoutMs());
        assertEquals(2, response.retryCount());
        assertEquals(30, response.checkIntervalSeconds());
        assertTrue(response.active());
    }
}
