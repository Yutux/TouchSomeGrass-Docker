package com.exemple.security.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusDto {
    private Integer userId;
    private String username;
    private UserStatus status;
    private Long lastSeen;
    
    public enum UserStatus {
        ONLINE, OFFLINE, AWAY
    }
}