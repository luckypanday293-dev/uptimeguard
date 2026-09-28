package com.uptimeguard.dto;

import java.util.List;

public record DashboardResponse(
        long totalMonitors,
        long upMonitors,
        long downMonitors,
        long openIncidents,
        long unreadNotifications,
        double averageResponseTimeMs,
        List<MonitorResponse> monitors,
        List<IncidentResponse> recentIncidents,
        List<NotificationResponse> recentNotifications
) {}
