package com.example.plantappbackend.service;

import com.example.plantappbackend.model.Plant;
import com.example.plantappbackend.repository.PlantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlantService {

    private final PlantRepository plantRepository;

    @Autowired
    public PlantService(PlantRepository plantRepository) {
        this.plantRepository = plantRepository;
    }

    // 특정 Plant ID로 조회
    public Plant getPlantById(Long id) {
        return plantRepository.findById(id).orElse(null);
    }

    // 특정 사용자의 식물 목록 조회
    public List<Plant> getPlantsByUser(String userUuid) {
        return plantRepository.findAllByUserUuid(userUuid);
    }

    // 식물 데이터 저장
    public Plant savePlant(Plant plant) {
        return plantRepository.save(plant);
    }
}