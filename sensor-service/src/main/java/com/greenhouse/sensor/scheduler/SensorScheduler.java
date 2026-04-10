package com.greenhouse.sensor.scheduler;

import com.greenhouse.sensor.client.AutomationClient;
import com.greenhouse.sensor.client.IotApiClient;
import com.greenhouse.sensor.client.ZoneClient;
import com.greenhouse.sensor.model.TelemetryReading;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class SensorScheduler {

    private static final Logger log = LoggerFactory.getLogger(SensorScheduler.class);

    private final ZoneClient zoneClient;
    private final IotApiClient iotApiClient;
    private final AutomationClient automationClient;

    private final CopyOnWriteArrayList<TelemetryReading> latestReadings = new CopyOnWriteArrayList<>();

    public SensorScheduler(ZoneClient zoneClient, IotApiClient iotApiClient, AutomationClient automationClient) {
        this.zoneClient = zoneClient;
        this.iotApiClient = iotApiClient;
        this.automationClient = automationClient;
    }

    @Scheduled(fixedDelay = 10000)
    public void pollTelemetry() {
        log.info("Polling telemetry for all zones...");
        try {
            List<Map<String, Object>> zones = zoneClient.getAllZones();
            latestReadings.clear();
            for (Map<String, Object> zone : zones) {
                String deviceId = (String) zone.get("deviceId");
                String zoneId = zone.get("id") != null ? zone.get("id").toString() : "unknown";
                if (deviceId == null || deviceId.isBlank()) continue;
                TelemetryReading reading = iotApiClient.fetchTelemetry(deviceId, zoneId);
                latestReadings.add(reading);
                log.info("Telemetry for zone {}: temp={}, humidity={}", zoneId,
                        reading.getTemperature(), reading.getHumidity());
                // Forward to automation service
                try {
                    automationClient.processTelemetry(reading);
                } catch (Exception e) {
                    log.warn("Failed to send telemetry to automation service: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn("Error during telemetry poll: {}", e.getMessage());
        }
    }

    public List<TelemetryReading> getLatestReadings() {
        return List.copyOf(latestReadings);
    }
}
