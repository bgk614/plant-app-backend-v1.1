package com.example.plantappbackend.repository;

import com.example.plantappbackend.model.Plant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlantRepository extends JpaRepository<Plant, Long> {
    /*특정 사용자의 UUID로 식물 목록 조회*/
    List<Plant> findAllByUserUuid(String userUuid);
}