package com.example.plantappbackend.controller;

import com.example.plantappbackend.DTO.PlantDTO;
import com.example.plantappbackend.DTO.PlantDTOWithRecentStatus;
import com.example.plantappbackend.DTO.PlantStatusDTO;
import com.example.plantappbackend.model.Plant;
import com.example.plantappbackend.model.PlantStatus;
import com.example.plantappbackend.service.PlantService;
import com.example.plantappbackend.service.PlantStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/plants")
public class PlantController {

    private final PlantService plantService;
    private final PlantStatusService plantStatusService;

    @Autowired
    public PlantController(PlantService plantService, PlantStatusService plantStatusService) {
        this.plantService = plantService;
        this.plantStatusService = plantStatusService;
    }
    // 특정 유저의 전체 식물 조회
    @GetMapping("/list/{userUuid}")
    public ResponseEntity<?> getPlantsByUserUuidWithRecentStatus(@PathVariable String userUuid) {
        try {
            // UUID로 식물 목록 조회 및 최신 상태 포함 DTO로 변환
            List<PlantDTOWithRecentStatus> plant = plantService.getPlantsByUserWithRecentStatus(userUuid);
            return ResponseEntity.ok(plant);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("조회 중 오류가 발생: " + e.getMessage());
        }
    }

    // 식물 추가
    @PostMapping("/create")
    public ResponseEntity<?> addPlantAndStatus(@RequestBody Map<String, Object> requestData) {
        try {
            // 요청 데이터 추출
            String userUuid = (String) requestData.get("userUuid");
            String plantNickname = (String) requestData.get("nickname");
            String name = (String) requestData.get("name");
            Integer checkDateInterval = (Integer) requestData.get("checkDateInterval");
            String status = (String) requestData.get("status");
            String remedy = (String) requestData.get("remedy");
            String imageUrl = (String) requestData.get("imageUrl");

            // 닉네임 중복 확인
            if (plantService.isDuplicateNickname(plantNickname, userUuid)) {
                return ResponseEntity.badRequest().body("등록 실패: 닉네임이 중복됩니다.");
            }

            // 1. Plant 엔터티 생성 및 저장
            Plant plant = new Plant();
            plant.setUserUuid(userUuid);
            plant.setPlantName(name);
            plant.setPlantNickname(plantNickname);
            plant.setCheckDateInterval(checkDateInterval);
            Plant savedPlant = plantService.savePlant(plant); // 저장 후 ID 생성

            // 2. PlantStatus 엔터티 생성 및 저장
            PlantStatus plantStatus = new PlantStatus();
            plantStatus.setPlant(savedPlant); // 저장된 Plant 엔터티 설정
            plantStatus.setRemedy(remedy);
            plantStatus.setStatus(status);
            plantStatus.setImageUrl(imageUrl);
            plantStatusService.savePlantStatus(plantStatus);

            // 성공 응답
            return ResponseEntity.ok("식물 등록 성공");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("등록 실패: " + e.getMessage());
        }
    }
    // 특정 식물의 상태 기록 전체 조회
    @GetMapping("/statuses/{plantId}")
    public ResponseEntity<?> getPlantStatusesByPlantId(@PathVariable Long plantId) {
        try {
            List<PlantStatus> statuses = plantStatusService.getStatusByPlantId(plantId);
            if (statuses.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("message", "해당 Plant ID에 상태 정보가 없습니다."));
            }
            // 엔터티를 DTO로 변환
            List<PlantStatusDTO> statusDTOs = statuses.stream()
                    .map(status -> {
                        PlantStatusDTO dto = new PlantStatusDTO();
                        dto.setId(status.getId());
                        dto.setStatus(status.getStatus());
                        dto.setRemedy(status.getRemedy());
                        dto.setImageUrl(status.getImageUrl());
                        dto.setCreatedAt(status.getCreatedAt());
                        return dto;
                    })
                    .collect(Collectors.toList());
            return ResponseEntity.ok(statusDTOs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "조회 실패", "error", e.getMessage()));
        }
    }

    // 특정 식물 조회
    @GetMapping("/{plantId}")
    public ResponseEntity<?> getPlantById(@PathVariable Long plantId) {
        try {
            PlantDTOWithRecentStatus plantDTO = plantService.getPlantWithRecentStatusById(plantId);
            if (plantDTO == null) {
                return ResponseEntity.status(404).body("해당 ID의 식물을 찾을 수 없습니다.");
            }
            return ResponseEntity.ok(plantDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("조회 중 오류가 발생: " + e.getMessage());
        }
    }

    // 특정 식물 삭제
    @DeleteMapping("/delete/{plantId}")
    public ResponseEntity<?> deletePlantById(@PathVariable Long plantId) {
        try {
            plantService.deletePlantById(plantId);
            return ResponseEntity.ok(Map.of("message", "성공적으로 삭제되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "삭제 중 오류가 발생했습니다.", "error", e.getMessage()));
        }
    }
}
