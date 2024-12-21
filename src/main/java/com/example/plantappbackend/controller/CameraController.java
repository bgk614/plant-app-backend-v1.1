package com.example.plantappbackend.controller;

import com.example.plantappbackend.service.CameraService;
import com.example.plantappbackend.service.PlantStatusService;
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
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class CameraController {

    private final CameraService cameraService;

    @Autowired
    public CameraController(CameraService cameraService, PlantStatusService plantService) {
        this.cameraService = cameraService;
    }

    // 새로운 식물 카메라 촬영할 때 (데이터 저장 없이, 결과값만 반환)
    @CrossOrigin(origins = "*")
    @PostMapping("/camera")
    public ResponseEntity<?> detectPlantNameAndStatus(@RequestParam("image") MultipartFile image) {
        try {
            // CameraService를 이용해 식물 이름, 상태, 대처법 감지
            Map<String, String> result = cameraService.detectPlantNameAndStatus(image);

            // 응답 데이터 구성
            Map<String, Object> response = new HashMap<>();
            response.put("name", result.getOrDefault("name", "알 수 없음"));
            response.put("status", result.getOrDefault("status", "알 수 없음"));
            response.put("remedy", result.getOrDefault("remedy", "알 수 없음"));
            response.put("imageUrl", result.getOrDefault("imageUrl", "알 수 없음"));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
