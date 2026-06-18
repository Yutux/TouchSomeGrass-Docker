package com.exemple.security.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.exemple.security.entities.Role;
import com.exemple.security.enums.RoleName;

public interface RoleRepository extends JpaRepository<Role, Long>{
	Optional<Role> findByRoleName(RoleName roleName);
	Optional<Role> findById(Integer id);
	boolean existsByRoleName(RoleName roleName);
}
