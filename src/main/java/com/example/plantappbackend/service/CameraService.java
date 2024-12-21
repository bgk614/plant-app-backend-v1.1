package com.example.plantappbackend.service;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
public class CameraService {

    private static final long MAX_IMAGE_SIZE = 1024 * 1024; // 이미지 최대 크기 1MB
    private final AwsS3Service awsS3Service;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${openai.api.key}")
    private String openAiApiKey;

    @Autowired
    public CameraService(AwsS3Service awsS3Service) {
        this.awsS3Service = awsS3Service;
    }

    // 이미지 크기 확인 및 반복 압축
    private byte[] compressImageUntilBelowLimit(byte[] imageBytes) {
        try {
            float quality = 0.8f; // 초기 압축 품질
            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));

            while (imageBytes.length > MAX_IMAGE_SIZE) {
                ByteArrayOutputStream compressedOutputStream = new ByteArrayOutputStream();

                Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
                ImageWriter writer = writers.next();

                ImageOutputStream outputStream = ImageIO.createImageOutputStream(compressedOutputStream);
                writer.setOutput(outputStream);

                ImageWriteParam params = writer.getDefaultWriteParam();
                params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                params.setCompressionQuality(quality);

                writer.write(null, new javax.imageio.IIOImage(originalImage, null, null), params);

                outputStream.close();
                writer.dispose();

                imageBytes = compressedOutputStream.toByteArray();

                System.out.println("압축 후 이미지 크기: " + imageBytes.length / 1024 + "KB");

                // 품질 감소
                quality -= 0.1f;
                if (quality < 0.1f) {
                    throw new IllegalArgumentException("이미지 압축 품질을 낮췄음에도 크기를 줄일 수 없습니다.");
                }
            }
            return imageBytes;
        } catch (IOException e) {
            throw new RuntimeException("이미지 압축 중 오류 발생", e);
        }
    }

    // 이미지 크기 확인 후 압축 실행
    private byte[] validateAndCompressImage(MultipartFile image) {
        try {
            byte[] imageBytes = image.getBytes();
            if (imageBytes.length > MAX_IMAGE_SIZE) {
                System.out.println("이미지 크기가 1MB를 초과하여 압축합니다.");
                imageBytes = compressImageUntilBelowLimit(imageBytes);
            }
            return imageBytes;
        } catch (IOException e) {
            throw new RuntimeException("이미지 처리 중 오류 발생", e);
        }
    }

    // GPT API 호출
    private Map<String, String> callGptApi(String base64Image, List<Map<String, Object>> messages) throws IOException {
        String gptEndpoint = "https://api.openai.com/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + openAiApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o");
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.0);

        // 요청 데이터 로그 출력
        System.out.println("GPT 요청 데이터: " + requestBody);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(gptEndpoint, requestEntity, Map.class);

        // 응답 데이터 로그 출력
        System.out.println("GPT 응답 데이터: " + response.getBody());

        Map responseBody = response.getBody();
        if (responseBody != null && responseBody.containsKey("choices")) {
            List<Map> choices = (List<Map>) responseBody.get("choices");
            if (!choices.isEmpty()) {
                Map choice = choices.get(0);
                Map message = (Map) choice.get("message");
                if (message != null && message.containsKey("content")) {
                    String content = (String) message.get("content");
                    return parseResponseContent(content);
                }
            }
        }
        return Map.of("name", "식별 실패", "status", "분석 실패");
    }

    // 식물 분석 공통 로직
    private Map<String, String> detectPlant(MultipartFile image, String systemPrompt, String userPrompt) {
        try {
            // 이미지 압축 및 검증
            byte[] compressedImage = validateAndCompressImage(image);
            // 이미지 업로드 후 URL 가져오기
            String fileUrl = awsS3Service.uploadFile(image);
            // Base64 인코딩된 이미지 생성
            String base64Image = "data:image/jpeg;base64," + Base64.encodeBase64String(compressedImage);

            // GPT 메시지 생성
            List<Map<String, Object>> messages = List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", List.of(
                            Map.of("type", "text", "text", userPrompt),
                            Map.of("type", "image_url", "image_url", Map.of("url", base64Image))
                    ))
            );

            // GPT API 호출
            Map<String, String> gptResult = callGptApi(base64Image, messages);

            // 결과에 S3 URL 추가
            Map<String, String> result = new HashMap<>(gptResult);
            result.put("imageUrl", fileUrl);

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("error", "처리 중 오류 발생: " + e.getMessage());
        }
    }
    // 식물 이름 및 상태 요청
    public Map<String, String> detectPlantNameAndStatus(MultipartFile image) {
        return detectPlant(image,
                "당신은 식물의 이름, 상태, 질병 여부 및 대처법을 알려주는 친절한 도우미입니다. 대답은 아래 형식을 따르세요:\n"
                        + "이름: [식물 이름]\n"
                        + "상태: [식물 상태]\n"
                        + "대처법: [친절한 말투로 설명한 대처 방법]",
                "이 이미지는 식물입니다. 식물의 이름과 상태를 분석하여 아래 형식에 맞춰 알려주세요:\n"
                        + "- 상태는 색상, 잎 모양, 손상 여부 등 자세히 작성해주세요.\n"
                        + "- 대처법은 시각장애인을 위한 앱이므로, 간결하면서도 친절한 말투로 작성해주세요.\n"
                        + "- 대답은 꼭 다음 형식을 지켜주세요:\n"
                        + "이름: [식물 이름]\n"
                        + "상태: [상태 설명]\n"
                        + "대처법: [친절한 말투로 대처법]");
    }

    // GPT 응답 파싱
    private Map<String, String> parseResponseContent(String content) {
        String name = "이름 정보 없음";
        String status = "상태 정보 없음";
        String remedy = "대처법 정보 없음";

        // 각 줄을 "\n" 기준으로 분리
        String[] lines = content.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("이름:")) {
                name = line.replace("이름:", "").trim();
            } else if (line.startsWith("상태:")) {
                status = line.replace("상태:", "").trim();
            } else if (line.startsWith("대처법:")) {
                remedy = line.replace("대처법:", "").trim();
            }
        }

        // 결과 반환
        return Map.of(
                "name", name,
                "status", status,
                "remedy", remedy
        );
    }}
