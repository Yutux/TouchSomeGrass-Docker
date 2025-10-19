package com.exemple.security.services;

import java.util.List;

import com.exemple.security.entities.Role;
import com.exemple.security.entities.UserApp;

public interface AccountService {
	UserApp addNewUser(UserApp appUser);
	Role addNewRole(Role appRole);
	void createRoleIfNotExists();
	void addRoleToUser(UserApp appUser, List<Role> roleNames);
}
