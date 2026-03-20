package lk.ijse.zonemanagementservice.controller;

import lk.ijse.zonemanagementservice.entity.Zone;
import lk.ijse.zonemanagementservice.service.ZoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/zones")
public class ZoneController {
    @Autowired
    private ZoneService zoneService;

    @PostMapping
    public ResponseEntity<Zone> createZone(@RequestBody Zone zone) {
        return ResponseEntity.ok(zoneService.saveZone(zone));
    }

    @GetMapping
    public ResponseEntity<List<Zone>> getAllZones() {
        return ResponseEntity.ok(zoneService.getAllZones());
    }
}
