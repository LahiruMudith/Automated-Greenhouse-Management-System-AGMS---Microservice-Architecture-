package com.greenhouse.automation.service;

import com.greenhouse.automation.client.ZoneClient;
import com.greenhouse.automation.dto.TelemetryPayload;
import com.greenhouse.automation.model.AutomationLog;
import com.greenhouse.automation.repository.AutomationLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AutomationService {

    private static final Logger log = LoggerFactory.getLogger(AutomationService.class);

    private final AutomationLogRepository logRepository;
    private final ZoneClient zoneClient;

    public AutomationService(AutomationLogRepository logRepository, ZoneClient zoneClient) {
        this.logRepository = logRepository;
        this.zoneClient = zoneClient;
    }

    public AutomationLog processTelemetry(TelemetryPayload payload) {
        Long zoneId = parseZoneId(payload.getZoneId());
        String action = "NO_ACTION";

        try {
            Map<String, Object> zone = zoneClient.getZone(zoneId);
            Double minTemp = parseDouble(zone.get("minTemp"));
            Double maxTemp = parseDouble(zone.get("maxTemp"));
            Double currentTemp = payload.getTemperature();

            if (currentTemp > maxTemp) {
                action = "TURN_FAN_ON";
                log.info("Zone {}: temp {} > maxTemp {} => {}", zoneId, currentTemp, maxTemp, action);
            } else if (currentTemp < minTemp) {
                action = "TURN_HEATER_ON";
                log.info("Zone {}: temp {} < minTemp {} => {}", zoneId, currentTemp, minTemp, action);
            } else {
                log.info("Zone {}: temp {} within range [{},{}] => {}", zoneId, currentTemp, minTemp, maxTemp, action);
            }
        } catch (Exception e) {
            log.warn("Could not fetch zone thresholds for zone {}: {}", payload.getZoneId(), e.getMessage());
            action = "THRESHOLD_FETCH_ERROR";
        }

        AutomationLog entry = new AutomationLog(
                payload.getZoneId(),
                payload.getDeviceId(),
                payload.getTemperature(),
                payload.getHumidity(),
                action
        );
        return logRepository.save(entry);
    }

    public List<AutomationLog> getAllLogs() {
        return logRepository.findAll();
    }

    private Long parseZoneId(String zoneId) {
        if (zoneId == null || zoneId.isBlank()) {
            throw new IllegalArgumentException("zoneId is null or blank");
        }
        return Long.parseLong(zoneId);
    }

    private Double parseDouble(Object val) {
        if (val == null) return 0.0;
        if (val instanceof Number) return ((Number) val).doubleValue();
        try { return Double.parseDouble(val.toString()); } catch (Exception e) { return 0.0; }
    }
}
