package com.exemple.security.dtos;

import java.util.List;

import com.exemple.security.entities.UserApp;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SpotDto {
	private int id;
	private String name;
    private String description;
    private double latitude;
    private double longitude;
    private String imagePath;
    private List<String> imageUrls;
    private String creator;
}
