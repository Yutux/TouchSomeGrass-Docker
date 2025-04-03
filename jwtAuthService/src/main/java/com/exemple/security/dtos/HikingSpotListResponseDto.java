package com.exemple.security.dtos;

import java.util.List;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HikingSpotListResponseDto {

	private List<HikingSpotDto> HikingSpots;
	private String message;
}
