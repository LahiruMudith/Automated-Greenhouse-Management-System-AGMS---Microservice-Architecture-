package com.greenhouse.sensor.client;

import com.greenhouse.sensor.model.TelemetryReading;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class IotApiClient {

    private static final Logger log = LoggerFactory.getLogger(IotApiClient.class);

    @Value("${iot.api.base-url:http://localhost:9090/api}")
    private String baseUrl;

    @Value("${iot.api.username:lahiru}")
    private String username;

    @Value("${iot.api.password:123456}")
    private String password;

    private final RestTemplate restTemplate = new RestTemplate();
    private final AtomicReference<String> accessToken = new AtomicReference<>();

    public void login() {
        try {
            Map<String, String> body = Map.of("username", username, "password", password);
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    baseUrl + "/auth/login", body, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                accessToken.set((String) response.getBody().get("accessToken"));
                log.info("IoT API login successful");
            }
        } catch (Exception e) {
            log.warn("IoT API login failed: {}", e.getMessage());
        }
    }

    public TelemetryReading fetchTelemetry(String deviceId, String zoneId) {
        if (accessToken.get() == null) {
            login();
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken.get());
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    baseUrl + "/devices/telemetry/" + deviceId,
                    HttpMethod.GET, entity, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> data = response.getBody();
                Double temp = parseDouble(data.get("temperature"));
                Double humidity = parseDouble(data.get("humidity"));
                return new TelemetryReading(deviceId, zoneId, temp, humidity);
            }
        } catch (Exception e) {
            log.warn("Failed to fetch telemetry for device {}: {}. Using simulated data.", deviceId, e.getMessage());
        }
        // Simulated fallback
        return new TelemetryReading(deviceId, zoneId,
                20.0 + Math.random() * 15,
                50.0 + Math.random() * 30);
    }

    private Double parseDouble(Object val) {
        if (val == null) return 0.0;
        if (val instanceof Number) return ((Number) val).doubleValue();
        try { return Double.parseDouble(val.toString()); } catch (Exception e) { return 0.0; }
    }
}
