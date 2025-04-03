package com.exemple.security.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HikingSpotDto {
	private int id;
    private String name;
    private String description;
    private String region;
    private double distance;
    private String imagePath;
    private double startLatitude;
    private double startLongitude;
    private double endLatitude;
    private double endLongitude;
    private int difficultyLevel;
    private String creatorName;
}
