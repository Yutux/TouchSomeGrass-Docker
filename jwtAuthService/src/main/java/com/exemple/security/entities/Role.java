package com.exemple.security.entities;

import java.io.Serializable;

import org.springframework.security.core.GrantedAuthority;

import com.exemple.security.enums.RoleName;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class Role implements Serializable, GrantedAuthority {
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	int id;
	@Enumerated(EnumType.STRING)
	private RoleName roleName;
	
	public Role (RoleName roleName) {
		this.roleName = roleName;
	}
	
	public RoleName getRoleName() {
		return roleName;
	}
	
	@Override
	public String getAuthority() {
		return roleName.toString();
	}
}