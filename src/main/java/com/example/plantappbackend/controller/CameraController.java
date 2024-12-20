package com.example.plantappbackend.controller;

import com.example.plantappbackend.model.Plant;
import com.example.plantappbackend.service.CameraService;
import com.example.plantappbackend.service.PlantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/camera")
public class CameraController {

    private final CameraService cameraService;
    private final PlantService plantService;

    @Autowired
    public CameraController(CameraService cameraService, PlantService plantService) {
        this.cameraService = cameraService;
        this.plantService = plantService;
    }

    // OpenAI API 식물 이름 인식 (상태) 및 데이터 저장
    @PostMapping("/status")
    public ResponseEntity<?> detectPlantNameAndSave(@RequestParam("image") MultipartFile image, @RequestParam("userUuid") String userUuid) {
        try {
            // OpenAI를 이용해 식물 이름, 상태, 대처법 감지
            Map<String, String> result = cameraService.detectPlantNameAndStatus(image);

            // 감지된 데이터를 Plant 엔터티로 변환
            Plant plant = new Plant();
            plant.setName(result.getOrDefault("name", "알 수 없음"));
            plant.setStatus(result.getOrDefault("status", "알 수 없음"));
            plant.setRemedy(result.getOrDefault("remedy", "알 수 없음"));
            plant.setImageUrl(result.getOrDefault("imageUrl", null)); // 이미지 URL 설정
            plant.setUserUuid(userUuid); // 사용자의 UUID 설정

            // 데이터베이스에 저장
            Plant savedPlant = plantService.savePlant(plant);

            // 응답 데이터 구성
            Map<String, Object> response = new HashMap<>();
            response.put("detectedData", result); // 감지된 데이터
            response.put("savedPlant", savedPlant); // 저장된 데이터

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}