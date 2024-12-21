package com.example.plantappbackend.DTO;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PlantStatusDTO {
    private Long id;
    private String status;
    private String remedy;
    private String imageUrl;
    private LocalDateTime createdAt;
}
