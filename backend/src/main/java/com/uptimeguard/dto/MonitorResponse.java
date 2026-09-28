package com.uptimeguard.dto;

import com.uptimeguard.model.MonitorStatus;
import java.time.Instant;

public record MonitorResponse(
        Long id,
        String name,
        String url,
        Integer expectedStatus,
        Integer timeoutMs,
        Integer retryCount,
        Integer checkIntervalSeconds,
        Boolean active,
        MonitorStatus currentStatus,
        Instant lastCheckedAt,
        Long lastResponseTimeMs,
        double uptimePercent
) {}
