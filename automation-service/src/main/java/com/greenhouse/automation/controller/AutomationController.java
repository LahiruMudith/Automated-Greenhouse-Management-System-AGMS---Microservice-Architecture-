package com.greenhouse.automation.controller;

import com.greenhouse.automation.dto.TelemetryPayload;
import com.greenhouse.automation.model.AutomationLog;
import com.greenhouse.automation.service.AutomationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/automation")
public class AutomationController {

    private final AutomationService automationService;

    public AutomationController(AutomationService automationService) {
        this.automationService = automationService;
    }

    @PostMapping("/process")
    public ResponseEntity<AutomationLog> process(@RequestBody TelemetryPayload payload) {
        return ResponseEntity.ok(automationService.processTelemetry(payload));
    }

    @GetMapping("/logs")
    public ResponseEntity<List<AutomationLog>> getLogs() {
        return ResponseEntity.ok(automationService.getAllLogs());
    }
}
