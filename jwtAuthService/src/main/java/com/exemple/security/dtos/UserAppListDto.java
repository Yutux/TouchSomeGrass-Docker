package com.exemple.security.dtos;

import java.util.List;

import com.exemple.security.entities.UserApp;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserAppListDto {
	private List<UserApp> userList;
	private String message;
}
