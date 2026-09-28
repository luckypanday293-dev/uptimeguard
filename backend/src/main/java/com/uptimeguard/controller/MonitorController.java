package com.uptimeguard.controller;

import com.uptimeguard.dto.CheckResultResponse;
import com.uptimeguard.dto.MonitorRequest;
import com.uptimeguard.dto.MonitorResponse;
import com.uptimeguard.service.CheckExecutionService;
import com.uptimeguard.service.MonitorService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/monitors")
public class MonitorController {
    private final MonitorService monitorService;
    private final CheckExecutionService checkExecutionService;

    public MonitorController(MonitorService monitorService, CheckExecutionService checkExecutionService) {
        this.monitorService = monitorService;
        this.checkExecutionService = checkExecutionService;
    }

    @GetMapping
    public List<MonitorResponse> all() {
        return monitorService.findAll();
    }

    @PostMapping
    public MonitorResponse create(@Valid @RequestBody MonitorRequest request) {
        return monitorService.create(request);
    }

    @PutMapping("/{id}")
    public MonitorResponse update(@PathVariable Long id, @Valid @RequestBody MonitorRequest request) {
        return monitorService.update(id, request);
    }

    @PostMapping("/{id}/check")
    public CheckResultResponse checkNow(@PathVariable Long id) {
        return checkExecutionService.runCheck(id);
    }
}
