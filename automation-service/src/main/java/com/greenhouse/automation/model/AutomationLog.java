package com.greenhouse.automation.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "automation_logs")
public class AutomationLog {

    @Id
    private String id;

    private String zoneId;
    private String deviceId;
    private Double temperature;
    private Double humidity;
    private String action;
    private LocalDateTime triggeredAt;

    public AutomationLog() {}

    public AutomationLog(String zoneId, String deviceId, Double temperature, Double humidity, String action) {
        this.zoneId = zoneId;
        this.deviceId = deviceId;
        this.temperature = temperature;
        this.humidity = humidity;
        this.action = action;
        this.triggeredAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getZoneId() { return zoneId; }
    public void setZoneId(String zoneId) { this.zoneId = zoneId; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }

    public Double getHumidity() { return humidity; }
    public void setHumidity(Double humidity) { this.humidity = humidity; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public LocalDateTime getTriggeredAt() { return triggeredAt; }
    public void setTriggeredAt(LocalDateTime triggeredAt) { this.triggeredAt = triggeredAt; }
}
