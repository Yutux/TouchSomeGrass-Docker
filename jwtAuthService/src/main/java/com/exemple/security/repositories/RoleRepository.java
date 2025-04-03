package com.exemple.security.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.exemple.security.entities.Role;
import com.exemple.security.enums.RoleName;

public interface RoleRepository extends JpaRepository<Role, Long>{
	Role findByRoleName(RoleName roleName);
}
