package com.example.plantappbackend.DTO;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PlantDTOWithRecentStatus {
    private Long id;
    private String plantNickname;
    private String plantName;
    private int checkDateInterval;
    private LocalDateTime createdAt;
    private PlantStatusDTO recentStatus; // 최근 상태만 포함
}