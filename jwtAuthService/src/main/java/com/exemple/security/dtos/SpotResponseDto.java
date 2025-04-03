package com.exemple.security.dtos;

import com.exemple.security.entities.Spot;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SpotResponseDto {
	private Spot newSpot;
	private String creatorname;
	private String message;
}
