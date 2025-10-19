package com.exemple.security.dtos;

import java.util.List;

import com.exemple.security.entities.HikingSpot;
import com.exemple.security.entities.HikingWaypoint;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HikingSpotResponseDto {
	private HikingSpot newHikingSpot;
	private String name;
	private String message;
}
