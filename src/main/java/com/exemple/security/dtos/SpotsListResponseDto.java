package com.exemple.security.dtos;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SpotsListResponseDto {
	private List<SpotDto> spots;
	private String message;
}
