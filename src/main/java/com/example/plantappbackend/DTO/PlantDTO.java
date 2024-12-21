package com.example.plantappbackend.DTO;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class PlantDTO {
    private Long id;
    private String plantNickname;
    private String plantName;
    private int checkDateInterval;
    private LocalDateTime createdAt;
    private List<PlantStatusDTO> statuses;
}