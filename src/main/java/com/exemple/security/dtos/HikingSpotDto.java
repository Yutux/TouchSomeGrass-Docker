package com.exemple.security.dtos;

import java.util.List;

import com.exemple.security.entities.HikingWaypoint;

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
    private List<String> imageUrls;
    private List<HikingWaypoint> waypoints;
    private String creatorName;
}
