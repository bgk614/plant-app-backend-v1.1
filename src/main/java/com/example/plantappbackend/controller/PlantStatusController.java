package com.example.plantappbackend.controller;

import com.example.plantappbackend.model.Plant;
import com.example.plantappbackend.model.PlantStatus;
import com.example.plantappbackend.service.PlantService;
import com.example.plantappbackend.service.PlantStatusService;
import com.example.plantappbackend.service.CameraService;
import com.example.plantappbackend.service.AwsS3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/plants")
public class PlantStatusController {
     private final PlantService plantService;
     private final PlantStatusService plantStatusService;
     private final CameraService cameraService;
     private final AwsS3Service awsS3Service;

     @Autowired
     public PlantStatusController(
             PlantService plantService,
             PlantStatusService plantStatusService,
             CameraService cameraService,
             AwsS3Service awsS3Service) {
          this.plantService = plantService;
          this.plantStatusService = plantStatusService;
          this.cameraService = cameraService;
          this.awsS3Service = awsS3Service;
     }

     // 이미 등록되어 있는 식물 사진 찍었을 때 상태 정보 추가
     @PostMapping("/add/status/{plantId}")
     public ResponseEntity<?> addPlantStatus(@PathVariable Long plantId, @RequestParam("image") MultipartFile image) {
          try {
               // 1. 식물 ID로 기존 Plant 엔티티 조회
               Plant plant = plantService.getPlantById(plantId);
               if (plant == null) {
                    return ResponseEntity.status(404).body(Map.of("message", "해당 ID의 식물이 존재하지 않습니다."));
               }

               // 2. 이미지 S3 업로드 및 URL 가져오기
               String imageUrl = awsS3Service.uploadFile(image);

               // 3. OpenAI API를 사용해 식물 상태 분석
               Map<String, String> analysisResult = cameraService.detectPlantNameAndStatus(image);
               String name = analysisResult.getOrDefault("name", "알 수 없음");
               String status = analysisResult.getOrDefault("status", "알 수 없음");
               String remedy = analysisResult.getOrDefault("remedy", "알 수 없음");

               // 4. PlantStatus 엔티티 생성 및 저장
               PlantStatus plantStatus = new PlantStatus();
               plantStatus.setId(plantId);
               plantStatus.setPlant(plant); // 참조된 Plant 설정
               plantStatus.setImageUrl(imageUrl);
               plantStatus.setStatus(status);
               plantStatus.setRemedy(remedy);
               plantStatusService.savePlantStatus(plantStatus);

               // 5. 성공 응답 반환
               return ResponseEntity.ok(Map.of("message", "식물 상태가 성공적으로 추가되었습니다.", "status", status, "remedy", remedy, "imageUrl", imageUrl));
          } catch (Exception e) {
               return ResponseEntity.badRequest().body(Map.of("message", "식물 상태 추가 중 오류가 발생했습니다.", "error", e.getMessage()));
          }
     }

     // 특정 기록 삭제
     @DeleteMapping("/delete/status/{statusId}")
     public ResponseEntity<?> deletePlantStatus(@PathVariable Long statusId) {
          try {
               // 첫 번째 기록인지 확인
               if (plantStatusService.isFirstRecord(statusId)) {
                    return ResponseEntity.badRequest().body(Map.of("message", "첫 번째 기록은 삭제할 수 없습니다."));
               }

               // 상태 삭제
               plantStatusService.deletePlantStatusById(statusId);
               return ResponseEntity.ok(Map.of("message", "성공적으로 삭제되었습니다."));
          } catch (Exception e) {
               return ResponseEntity.badRequest().body(Map.of(
                       "message", "식물 상태 삭제 중 오류가 발생했습니다.",
                       "error", e.getMessage()
               ));
          }
     }
}
