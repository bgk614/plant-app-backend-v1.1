package com.example.plantappbackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Plant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private String remedy;

    @Column
    private String status;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "user_uuid")
    private String userUuid;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}