package com.exemple.security.dtos;

import java.util.List;

import com.exemple.security.entities.UserApp;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendsListResponseDto {

    @NotNull
    private List<UserApp> friends;
    
    @Min(0)
    private int count;
}
