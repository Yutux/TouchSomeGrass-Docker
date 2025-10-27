package com.exemple.security.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchMetadata {

    private String query;
    private Double centerLatitude;
    private Double centerLongitude;
    private Double radiusKm;
    private String region;
    private String creatorEmail;
}
