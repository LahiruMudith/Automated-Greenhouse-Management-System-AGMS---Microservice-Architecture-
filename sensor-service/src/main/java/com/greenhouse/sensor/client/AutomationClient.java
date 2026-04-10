package com.greenhouse.sensor.client;

import com.greenhouse.sensor.model.TelemetryReading;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "automation-service", path = "/api/automation")
public interface AutomationClient {

    @PostMapping("/process")
    void processTelemetry(@RequestBody TelemetryReading reading);
}
