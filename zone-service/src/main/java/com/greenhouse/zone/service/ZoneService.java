package com.greenhouse.zone.service;

import com.greenhouse.zone.client.IotApiClient;
import com.greenhouse.zone.model.Zone;
import com.greenhouse.zone.repository.ZoneRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ZoneService {

    private final ZoneRepository zoneRepository;
    private final IotApiClient iotApiClient;

    public ZoneService(ZoneRepository zoneRepository, IotApiClient iotApiClient) {
        this.zoneRepository = zoneRepository;
        this.iotApiClient = iotApiClient;
    }

    public Zone createZone(Zone zone) {
        System.out.println("ZoneService: Creating zone - " + zone);
        if (zone.getMinTemp() >= zone.getMaxTemp()) {
            System.out.println("ZoneService: Validation failed - minTemp (" + zone.getMinTemp() + ") >= maxTemp (" + zone.getMaxTemp() + ")");
            throw new IllegalArgumentException("minTemp must be less than maxTemp");
        }
        // Register device via IoT API
        System.out.println("ZoneService: Registering device for zone: " + zone.getName());
        String deviceId = iotApiClient.registerDevice(zone.getName());
        System.out.println("ZoneService: Device registered with ID: " + deviceId);
        zone.setDeviceId(deviceId);
        Zone savedZone = zoneRepository.save(zone);
        System.out.println("ZoneService: Zone saved successfully with ID: " + savedZone.getId());
        return savedZone;
    }

    public Zone getZone(Long id) {
        return zoneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Zone not found: " + id));
    }

    public List<Zone> getAllZones() {
        return zoneRepository.findAll();
    }

    public Zone updateZone(Long id, Zone updated) {
        Zone zone = getZone(id);
        if (updated.getMinTemp() >= updated.getMaxTemp()) {
            throw new IllegalArgumentException("minTemp must be less than maxTemp");
        }
        zone.setName(updated.getName());
        zone.setMinTemp(updated.getMinTemp());
        zone.setMaxTemp(updated.getMaxTemp());
        zone.setDescription(updated.getDescription());
        return zoneRepository.save(zone);
    }

    public void deleteZone(Long id) {
        getZone(id); // validates existence; throws if not found
        zoneRepository.deleteById(id);
    }
}
