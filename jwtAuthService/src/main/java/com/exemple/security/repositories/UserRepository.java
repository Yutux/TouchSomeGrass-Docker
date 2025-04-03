package com.exemple.security.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import com.exemple.security.entities.UserApp;

public interface UserRepository extends JpaRepository<UserApp, Integer> {
	 Optional<UserApp> findByEmail(String email);
}
