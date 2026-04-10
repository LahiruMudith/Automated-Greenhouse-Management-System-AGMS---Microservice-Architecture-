package com.greenhouse.zone.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;
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

    public String registerDevice(String zoneName) {
        if (accessToken.get() == null) {
            login();
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken.get());
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, String> body = Map.of("name", zoneName, "type", "greenhouse-zone");
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    baseUrl + "/devices", entity, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object id = response.getBody().get("id");
                return id != null ? id.toString() : UUID.randomUUID().toString();
            }
        } catch (Exception e) {
            log.warn("IoT device registration failed: {}. Using generated deviceId.", e.getMessage());
        }
        return UUID.randomUUID().toString();
    }
}
