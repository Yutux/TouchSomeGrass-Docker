package com.exemple.security.dtos;

import com.exemple.security.entities.HikingSpot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HikingSpotResponseDto {
	private HikingSpot newHikingSpot;
	private String name;
	private String message;
}
