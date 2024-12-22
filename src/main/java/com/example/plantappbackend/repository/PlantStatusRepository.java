package com.example.plantappbackend.repository;

import com.example.plantappbackend.model.Plant;
import com.example.plantappbackend.model.PlantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PlantStatusRepository extends JpaRepository<PlantStatus, Long> {
     // 특정 Plant ID로 모든 상태 정보 조회
     List<PlantStatus> findByPlantId(Long plantId);

     // 특정 Plant ID로 최신 상태 조회
     PlantStatus findTopByPlantIdOrderByCreatedAtDesc(Long plantId);

     // 특정 Plant와 관련된 가장 오래된 상태 조회
     PlantStatus findFirstByPlantOrderByCreatedAtAsc(Plant plant);

     // 특정 Plant와 관련된 모든 상태 삭제
     @Transactional
     @Modifying
     @Query("DELETE FROM PlantStatus ps WHERE ps.plant.id = :plantId")
     void deleteByPlantId(@Param("plantId") Long plantId);
}
