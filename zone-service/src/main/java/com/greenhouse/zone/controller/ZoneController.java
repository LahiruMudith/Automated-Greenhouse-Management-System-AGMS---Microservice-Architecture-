package com.greenhouse.zone.controller;

import com.greenhouse.zone.model.Zone;
import com.greenhouse.zone.service.ZoneService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/zones")
public class ZoneController {

    private final ZoneService zoneService;

    public ZoneController(ZoneService zoneService) {
        this.zoneService = zoneService;
    }

    @PostMapping
    public ResponseEntity<Zone> createZone(@Valid @RequestBody Zone zone) {
        System.out.println("Creating new zone: " + zone);
        Zone createdZone = zoneService.createZone(zone);
        System.out.println("Successfully created zone with ID: " + createdZone.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdZone);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Zone> getZone(@PathVariable("id") Long id) {
        return ResponseEntity.ok(zoneService.getZone(id));
    }

    @GetMapping
    public ResponseEntity<List<Zone>> getAllZones() {
        return ResponseEntity.ok(zoneService.getAllZones());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Zone> updateZone(@PathVariable("id") Long id, @Valid @RequestBody Zone zone) {
        return ResponseEntity.ok(zoneService.updateZone(id, zone));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteZone(@PathVariable("id") Long id) {
        zoneService.deleteZone(id);
        return ResponseEntity.noContent().build();
    }
}
