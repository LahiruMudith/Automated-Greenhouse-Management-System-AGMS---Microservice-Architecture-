package com.greenhouse.crop.controller;

import com.greenhouse.crop.model.Crop;
import com.greenhouse.crop.service.CropService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/crops")
public class CropController {

    private final CropService cropService;

    public CropController(CropService cropService) {
        this.cropService = cropService;
    }

    @PostMapping
    public ResponseEntity<Crop> createCrop(@Valid @RequestBody Crop crop) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cropService.createCrop(crop));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable("id") Long id,
                                          @RequestBody Map<String, String> body) {
        String status = (body == null) ? null : body.get("status");
        System.out.println("updateStatus called - id: " + id + ", status: " + status);

        if (status == null || status.isBlank()) {
            System.out.println("updateStatus bad request - missing/blank status");
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Request body must contain a non-blank 'status' field"));
        }

        try {
            Object updated = cropService.updateStatus(id, status);
            System.out.println("updateStatus success - id: " + id + ", newStatus: " + status);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            // good for "invalid status value" cases
            System.out.println("updateStatus illegal argument - " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            // prevents generic 500 without info
            System.out.println("updateStatus error - " + e.getClass().getName() + ": " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Internal Server Error"));
        }
    }

    @GetMapping
    public ResponseEntity<List<Crop>> getAllCrops() {
        return ResponseEntity.ok(cropService.getAllCrops());
    }
}
