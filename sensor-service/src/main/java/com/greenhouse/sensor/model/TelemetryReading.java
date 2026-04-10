package com.greenhouse.sensor.model;

public class TelemetryReading {
    private String deviceId;
    private String zoneId;
    private Double temperature;
    private Double humidity;
    private Long timestamp;

    public TelemetryReading() {}

    public TelemetryReading(String deviceId, String zoneId, Double temperature, Double humidity) {
        this.deviceId = deviceId;
        this.zoneId = zoneId;
        this.temperature = temperature;
        this.humidity = humidity;
        this.timestamp = System.currentTimeMillis();
    }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getZoneId() { return zoneId; }
    public void setZoneId(String zoneId) { this.zoneId = zoneId; }

    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }

    public Double getHumidity() { return humidity; }
    public void setHumidity(Double humidity) { this.humidity = humidity; }

    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
}
