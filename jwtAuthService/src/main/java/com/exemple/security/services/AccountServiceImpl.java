package com.exemple.security.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

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
	@Transactional
	public void createRoleIfNotExists() {
		createRoleWithFixedId(1, RoleName.USER);
		createRoleWithFixedId(2, RoleName.ADMIN);
	}

	private void createRoleWithFixedId(int fixedId, RoleName roleName) {
		// ✅ Vérifier d'abord par ID
		Optional<Role> roleById = appRoleRepository.findById(fixedId);
		
		if (roleById.isPresent()) {
			Role role = roleById.get();
			// ✅ L'ID existe, vérifier si c'est le bon rôle
			if (role.getRoleName() != roleName) {
				// ⚠️ Conflit : ID existe mais avec un autre rôle !
				System.out.println("⚠️ Conflit détecté : ID " + fixedId + " existe avec " + role.getRoleName());
			} else {
				System.out.println("ℹ️ Rôle déjà existant : " + roleName + " (id=" + fixedId + ")");
			}
		} else {
			// ✅ Vérifier si le rôle existe avec un autre ID
			Optional<Role> roleByName = appRoleRepository.findByRoleName(roleName);
			
			if (roleByName.isPresent()) {
				Role existingRole = roleByName.get();
				// ⚠️ Le rôle existe mais avec un mauvais ID
				System.out.println("⚠️ Rôle " + roleName + " existe avec id=" + existingRole.getId() + 
								" au lieu de " + fixedId);
				System.out.println("💡 Supprimez la base et redémarrez pour corriger les IDs");
			} else {
				// ✅ Créer le rôle avec l'ID fixe
				Role role = new Role();
				role.setId(fixedId);
				role.setRoleName(roleName);
				appRoleRepository.save(role);
				System.out.println("✅ Rôle créé : " + roleName + " (id=" + fixedId + ")");
			}
		}
	}
	
	@Override
	public void addRoleToUser(UserApp user, List<Role> rolesName) {
		UserApp foundUser = appUserRepository.findByEmail(user.getEmail()).orElse(null);
		
		if (foundUser == null) {
			throw new RuntimeException("Utilisateur non trouvé : " + user.getEmail());
		}
		
		// ✅ Compatible Java 8
		rolesName.stream()
			.map(Role::getRoleName)
			.map(appRoleRepository::findByRoleName)
			.filter(Optional::isPresent)       // Garde seulement les Optional non-vides
			.map(Optional::get)                 // Extrait Role de Optional<Role>
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