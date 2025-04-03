package com.exemple.security.dtos;


import com.exemple.security.entities.UserApp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HikingSpotRequestDto {
	private int id;
	private String name;
    private String description;
    private String region;
    private double startLatitude;
    private double startLongitude;
    private double endLatitude;
    private double endLongitude;
    private String imagePath;
    private int creatorId;
    private UserApp creator;

}
