package com.greenhouse.sensor.controller;

import com.greenhouse.sensor.model.TelemetryReading;
import com.greenhouse.sensor.scheduler.SensorScheduler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sensors")
public class SensorController {

    private final SensorScheduler sensorScheduler;

    public SensorController(SensorScheduler sensorScheduler) {
        this.sensorScheduler = sensorScheduler;
    }

    @GetMapping("/latest")
    public ResponseEntity<List<TelemetryReading>> getLatest() {
        return ResponseEntity.ok(sensorScheduler.getLatestReadings());
    }
}
