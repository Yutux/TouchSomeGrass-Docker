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
public class HikingWaypointDto {
    private String name;
    private String address;
    private String placeId;
    private double latitude;
    private double longitude;
    private Double rating;
    private List<String> photos; // URLs Google Maps
}