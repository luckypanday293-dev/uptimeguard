package com.uptimeguard.service;

public record ProbeResult(
        boolean successful,
        Integer statusCode,
        long responseTimeMs,
        String errorMessage
) {}
