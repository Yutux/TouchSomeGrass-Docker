package com.exemple.security.dtos;

import java.util.ArrayList;
import java.util.List;

import com.exemple.security.entities.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestDto {
  private String firstname;
  private String lastname;
  private String email;
  private String password;
  private List<Role> roles = new ArrayList<>();
  
  public List<Role> setRoles(Role role) {
	  roles.add(role);
	  return roles;
  }
  
}

