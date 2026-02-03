package com.exemple.security.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.exemple.security.entities.HikingSpot;
import com.exemple.security.entities.Role;
import com.exemple.security.entities.Spot;
import com.exemple.security.entities.UserApp;
import com.exemple.security.enums.RoleName;
import com.exemple.security.repositories.RoleRepository;
import com.exemple.security.repositories.UserRepository;
import com.exemple.security.tools.CustomPasswordEncoder;

@Service
@Transactional
public class AccountServiceImpl implements AccountService, UserDetailsService {
	@Autowired
	private UserRepository appUserRepository;
	@Autowired
	private RoleRepository appRoleRepository;
	@Autowired
	private CustomPasswordEncoder passwordEncoder;

	@Override
	public UserApp addNewUser(UserApp user) {
		String pwd = user.getPassword();
		String pwdEncrypted = passwordEncoder.encode(pwd);
		user.setPassword(pwdEncrypted);
		return appUserRepository.save(user);
	}

	@Override
	public Role addNewRole(Role role) {
		return appRoleRepository.save(role);
	}

	@Override
	public void createRoleIfNotExists() {
		Role admin = new Role(); 
		Role user = new Role();
		
		admin.setRoleName(RoleName.ADMIN);
		user.setRoleName(RoleName.USER);
		
		addNewRole(user);
		addNewRole(admin);
	}
	
	@Override
	public void addRoleToUser(UserApp user, List<Role> rolesName) {
		UserApp foundUser = appUserRepository.findByEmail(user.getEmail()).orElse(null);
		rolesName.stream()
	    .map(Role::getRoleName)
	    .map(appRoleRepository::findByRoleName)
	    .forEach(foundUser.getRoles()::add);
		appUserRepository.save(foundUser);	 
	}
	
	@Override
	public UserDetails loadUserByUsername(String mail) throws UsernameNotFoundException {
		UserApp existingUser = appUserRepository.findByEmail(mail).orElse(null);
		if(existingUser == null) {
			throw new UsernameNotFoundException(mail);
		}
		
		Collection<GrantedAuthority> authorities = new ArrayList<>();
		existingUser.getRoles().forEach(role -> {
			authorities.add(new SimpleGrantedAuthority(role.getRoleName().toString()));
		});		

		return new User(existingUser.getUsername(), existingUser.getPassword(), authorities);
	}
	
	public List<Spot> getAllUserSpot(UserApp user) {
		return user.getSpots();
	}
	
	public List<HikingSpot> getAllUserHikingSpot(UserApp user){
		return user.getHikingSpots();
	}

	// ==========================================
	// 🆕 NOUVELLES MÉTHODES UTILITAIRES
	// ==========================================

	/**
	 * Récupère l'ID de l'utilisateur actuellement connecté depuis le JWT
	 * @return L'ID de l'utilisateur connecté
	 * @throws RuntimeException si l'utilisateur n'est pas authentifié
	 */
	public Integer getCurrentUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new RuntimeException("User not authenticated");
		}

		// Le nom d'utilisateur dans le JWT est l'email
		String email = authentication.getName();
		
		if (email == null || email.equals("anonymousUser")) {
			throw new RuntimeException("User not authenticated");
		}

		// Récupérer l'utilisateur depuis la base de données
		UserApp user = appUserRepository.findByEmail(email)
			.orElseThrow(() -> new RuntimeException("User not found with email: " + email));
		
		return user.getId();
	}

	/**
	 * Récupère l'utilisateur complet actuellement connecté
	 * @return L'utilisateur connecté
	 * @throws RuntimeException si l'utilisateur n'est pas authentifié
	 */
	public UserApp getCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new RuntimeException("User not authenticated");
		}

		String email = authentication.getName();
		
		if (email == null || email.equals("anonymousUser")) {
			throw new RuntimeException("User not authenticated");
		}

		return appUserRepository.findByEmail(email)
			.orElseThrow(() -> new RuntimeException("User not found with email: " + email));
	}

	/**
	 * Vérifie si l'utilisateur connecté a un rôle spécifique
	 * @param roleName Le nom du rôle à vérifier
	 * @return true si l'utilisateur a ce rôle
	 */
	public boolean currentUserHasRole(RoleName roleName) {
		try {
			UserApp user = getCurrentUser();
			return user.getRoles().stream()
				.anyMatch(role -> role.getRoleName() == roleName);
		} catch (RuntimeException e) {
			return false;
		}
	}

	/**
	 * Vérifie si l'utilisateur connecté est un administrateur
	 * @return true si l'utilisateur est admin
	 */
	public boolean isCurrentUserAdmin() {
		return currentUserHasRole(RoleName.ADMIN);
	}
}