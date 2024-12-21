package com.example.plantappbackend.repository;

import com.example.plantappbackend.model.PlantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlantStatusRepository extends JpaRepository<PlantStatus, Long> {
    // 특정 Plant ID로 모든 상태 정보 조회
    List<PlantStatus> findByPlantId(Long plantId);
    // 특정 Plant ID로 최신 상태 조회
    PlantStatus findTopByPlantIdOrderByCreatedAtDesc(Long plantId);
}
