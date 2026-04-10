package com.greenhouse.crop.service;

import com.greenhouse.crop.model.Crop;
import com.greenhouse.crop.model.CropStatus;
import com.greenhouse.crop.repository.CropRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CropService {

    private final CropRepository cropRepository;

    public CropService(CropRepository cropRepository) {
        this.cropRepository = cropRepository;
    }

    public Crop createCrop(Crop crop) {
        crop.setStatus(CropStatus.SEEDLING);
        return cropRepository.save(crop);
    }

    public Crop updateStatus(Long id, String statusStr) {
        Crop crop = cropRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Crop not found: " + id));
        CropStatus newStatus = CropStatus.valueOf(statusStr.toUpperCase());
        validateTransition(crop.getStatus(), newStatus);
        crop.setStatus(newStatus);
        return cropRepository.save(crop);
    }

    public List<Crop> getAllCrops() {
        return cropRepository.findAll();
    }

    private void validateTransition(CropStatus current, CropStatus next) {
        boolean valid = switch (current) {
            case SEEDLING -> next == CropStatus.VEGETATIVE;
            case VEGETATIVE -> next == CropStatus.HARVESTED;
            case HARVESTED -> false;
        };
        if (!valid) {
            throw new IllegalStateException(
                    "Invalid transition: " + current + " -> " + next);
        }
    }
}
