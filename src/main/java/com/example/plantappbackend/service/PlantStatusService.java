package com.example.plantappbackend.service;

import com.example.plantappbackend.model.PlantStatus;
import com.example.plantappbackend.repository.PlantStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlantStatusService {

     private final PlantStatusRepository plantStatusRepository;

     @Autowired
     public PlantStatusService(PlantStatusRepository plantStatusRepository) {
          this.plantStatusRepository = plantStatusRepository;
     }

     // PlantStatus 저장 메서드
     public PlantStatus savePlantStatus(PlantStatus plantStatus) {
          return plantStatusRepository.save(plantStatus);
     }

     // Plant ID로 상태 조회 메서드
     public List<PlantStatus> getStatusByPlantId(Long plantId) {
          return plantStatusRepository.findByPlantId(plantId);
     }

     public void deletePlantStatusById(Long statusId) {
          // ID를 기준으로 삭제
          plantStatusRepository.deleteById(statusId);
     }
}
