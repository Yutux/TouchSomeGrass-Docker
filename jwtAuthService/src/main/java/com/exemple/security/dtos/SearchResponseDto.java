package com.exemple.security.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponseDto<T> {
    private String message;
    private List<T> results;
    private int totalResults;
    private int page;
    private int totalPages;
    private SearchMetadata metadata;
}

