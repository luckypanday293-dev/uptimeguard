package com.uptimeguard.dto;

import com.uptimeguard.model.IncidentStatus;
import java.time.Instant;

public record IncidentResponse(
        Long id,
        Long monitorId,
        String monitorName,
        IncidentStatus status,
        Instant startedAt,
        Instant recoveredAt,
        Long outageDurationSeconds,
        String failureMessage
) {}
