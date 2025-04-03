package com.exemple.security.dtos;

import java.util.List;

import com.exemple.security.entities.HikingSpot;
import com.exemple.security.entities.Spot;
import com.exemple.security.entities.UserApp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponseDto {
  private String message;
  private String token;
  private UserApp userApp;
  private List<HikingSpot> userHikingSpots;
  private List<Spot> userSpots;
}
