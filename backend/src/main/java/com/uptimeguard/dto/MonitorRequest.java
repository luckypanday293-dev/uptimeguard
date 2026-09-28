package com.uptimeguard.dto;

import jakarta.validation.constraints.*;

public record MonitorRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Pattern(regexp = "https?://.+", message = "url must start with http:// or https://") String url,
        @Min(100) @Max(599) Integer expectedStatus,
        @Min(250) @Max(30000) Integer timeoutMs,
        @Min(0) @Max(5) Integer retryCount,
        @Min(5) @Max(86400) Integer checkIntervalSeconds,
        Boolean active
) {}
