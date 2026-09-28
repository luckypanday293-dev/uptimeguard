package com.uptimeguard.controller;

import com.uptimeguard.dto.CheckResultResponse;
import com.uptimeguard.dto.IncidentResponse;
import com.uptimeguard.dto.NotificationResponse;
import com.uptimeguard.model.IncidentStatus;
import com.uptimeguard.service.HistoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class HistoryController {
    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping("/checks")
    public List<CheckResultResponse> checks(
            @RequestParam(required = false) Long monitorId,
            @RequestParam(defaultValue = "50") int limit) {
        return historyService.checks(monitorId, limit);
    }

    @GetMapping("/incidents")
    public List<IncidentResponse> incidents(@RequestParam(required = false) IncidentStatus status) {
        return historyService.incidents(status);
    }

    @GetMapping("/notifications")
    public List<NotificationResponse> notifications(@RequestParam(defaultValue = "25") int limit) {
        return historyService.notifications(limit);
    }

    @PatchMapping("/notifications/{id}/read")
    public NotificationResponse markRead(@PathVariable Long id) {
        return historyService.markNotificationRead(id);
    }
}
