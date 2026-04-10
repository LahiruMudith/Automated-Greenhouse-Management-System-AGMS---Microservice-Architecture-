package com.greenhouse.sensor.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@FeignClient(name = "zone-service", path = "/api/zones")
public interface ZoneClient {

    @GetMapping
    List<Map<String, Object>> getAllZones();
}
