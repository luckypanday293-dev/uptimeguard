package com.uptimeguard.dto;

import java.time.Instant;

public record CheckResultResponse(
        Long id,
        Long monitorId,
        String monitorName,
        Instant checkedAt,
        Boolean successful,
        Integer statusCode,
        Long responseTimeMs,
        Integer attempts,
        String errorMessage
) {}
