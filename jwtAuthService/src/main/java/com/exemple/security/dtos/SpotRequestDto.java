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
public class SpotRequestDto {
	private String name;
    private String description;
    private double latitude;
    private double longitude;
    private String imagePath;  
    private UserApp creator;
}
