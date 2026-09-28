package com.uptimeguard.dto;

import com.uptimeguard.model.NotificationType;
import java.time.Instant;

public record NotificationResponse(
        Long id,
        Long monitorId,
        String monitorName,
        Long incidentId,
        NotificationType type,
        String message,
        Instant createdAt,
        Boolean read
) {}
