package com.example.plantappbackend.controller;

import com.example.plantappbackend.model.Plant;
import com.example.plantappbackend.service.PlantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plants")
public class PlantController {

    private final PlantService plantService;

    @Autowired
    public PlantController(PlantService plantService) {
        this.plantService = plantService;
    }

    /*
    read
    *이미지상세정보페이
     */


    /*특정 사용자*/
    @GetMapping("/user/{userUuid}")
    public ResponseEntity<?> getPlantsByUserUuid(@PathVariable String userUuid) {
        try {
            // UUID로 식물 목록 조회
            List<Plant> plants = plantService.getPlantsByUser(userUuid);
            return ResponseEntity.ok(plants);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("조회 중 오류가 발생: " + e.getMessage());
        }
    }

    /*새로운 식물 데이터 생성*/
    @PostMapping("/create")
    public ResponseEntity<?> addPlant(@RequestBody Plant plant) {
        try {
            Plant savedPlant = plantService.savePlant(plant);
            return ResponseEntity.ok(savedPlant);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /*특정 Plant ID로 정보 조회*/
    @GetMapping("/{id}")
    public ResponseEntity<?> getPlantById(@PathVariable Long id) {
        try {
            Plant plant = plantService.getPlantById(id);
            if (plant == null) {
                return ResponseEntity.status(404).body("해당 ID의 식물을 찾을 수 없습니다.");
            }
            return ResponseEntity.ok(plant);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("조회 중 오류가 발생: " + e.getMessage());
        }
    }
}