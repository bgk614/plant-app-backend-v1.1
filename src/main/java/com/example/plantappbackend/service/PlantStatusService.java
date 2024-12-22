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

     // Status ID로 상태 삭제
     public void deletePlantStatusById(Long statusId) {
          plantStatusRepository.deleteById(statusId);
     }

     // 첫 번째 기록 여부 확인
     public boolean isFirstRecord(Long statusId) {
          // 주어진 상태 ID에 해당하는 PlantStatus 조회
          PlantStatus plantStatus = plantStatusRepository.findById(statusId)
                  .orElseThrow(() -> new IllegalArgumentException("해당 ID의 상태가 존재하지 않습니다."));

          // 해당 Plant와 연결된 가장 오래된 상태 조회
          PlantStatus oldestStatus = plantStatusRepository.findFirstByPlantOrderByCreatedAtAsc(plantStatus.getPlant());

          // 현재 ID가 가장 오래된 상태의 ID인지 확인
          return oldestStatus.getId().equals(statusId);
     }
}
