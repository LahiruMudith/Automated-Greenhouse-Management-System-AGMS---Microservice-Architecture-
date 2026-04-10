package com.greenhouse.automation.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "zone-service", path = "/api/zones")
public interface ZoneClient {

    @GetMapping("/{id}")
    Map<String, Object> getZone(@PathVariable("id") Long id);
}
