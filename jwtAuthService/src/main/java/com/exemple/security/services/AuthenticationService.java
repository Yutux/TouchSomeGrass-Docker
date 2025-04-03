package com.exemple.security.services;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.exemple.security.config.JwtService;
import com.exemple.security.dtos.AuthenticationRequestDto;
import com.exemple.security.dtos.AuthenticationResponseDto;
import com.exemple.security.dtos.RegisterRequestDto;
import com.exemple.security.dtos.UserAppListDto;
import com.exemple.security.entities.HikingSpot;
import com.exemple.security.entities.Spot;
import com.exemple.security.entities.UserApp;
import com.exemple.security.enums.RoleName;
import com.exemple.security.repositories.RoleRepository;
import com.exemple.security.repositories.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthenticationService {
	private final UserRepository repository;
	private final RoleRepository roleRepository;
	private final JwtService jwtService;
	private final AuthenticationManager authenticationManager;
	private final AccountService accountService;
	private RoleName defaultRole = RoleName.USER;
	
	public ResponseEntity<AuthenticationResponseDto> register(RegisterRequestDto request) {
		if (repository.findByEmail(request.getEmail()).isPresent()) {
			return ResponseEntity.badRequest()
					.body(AuthenticationResponseDto.builder()
					.message("Un utilisateur avec cet e-mail existe déjà.")
					.build());
		}
		var user = UserApp.builder()
				.firstname(request.getFirstname())
				.lastname(request.getLastname())
				.email(request.getEmail())
				.password(request.getPassword())
				.roles(request.setRoles(roleRepository.findByRoleName(defaultRole)))
				.build();
		
		
		accountService.addNewUser(user);
		var jwtToken = jwtService.generateToken(user);

		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Authorization", "Bearer " + jwtToken);
		
		return ResponseEntity.ok()
				.headers(responseHeaders)
				.body(AuthenticationResponseDto.builder()
				.message("Enregistrement avec succes.")
				.build());
	}

	public ResponseEntity<AuthenticationResponseDto> authenticate(AuthenticationRequestDto request) {
	
		Authentication authentication = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
		SecurityContextHolder.getContext().setAuthentication(authentication);
		
		var user = repository.findByEmail(request.getEmail())
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));
		var jwtToken = jwtService.generateToken(user);
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Authorization", "Bearer " + jwtToken);
		return ResponseEntity.ok()
				.headers(responseHeaders)
				.body(AuthenticationResponseDto.builder()
						.message("User registered with success")
						.token(jwtToken)
						.build());
	}
	
	public ResponseEntity<UserAppListDto> getAllUsers(){
		List<UserApp> userList = repository.findAll();
		
		return ResponseEntity.ok().body(UserAppListDto.builder()
				.message("Look at them running around")
				.userList(userList)
				.build()
				);
	}
	
	public ResponseEntity<AuthenticationResponseDto> getUserById(int id){
		var user = repository.findById(id).orElseThrow(()-> new UsernameNotFoundException("User Not found"));
		
		return ResponseEntity.ok().body(AuthenticationResponseDto.builder()
				.message("User found here he is")
				.userApp(user)
				.build()
				);
	}
	
	public ResponseEntity<AuthenticationResponseDto> delete(int id){
		var user = repository.findById(id).orElseThrow(()-> new UsernameNotFoundException("User Not found"));
		
		repository.delete(user);
		return ResponseEntity.ok().body(AuthenticationResponseDto.builder()
				.message("Good he is gone")
				.build()
				);
	}
	/*
	public ResponseEntity<AuthenticationResponseDto> update(RegisterRequestDto req){
		var user = repository.findByEmail(req.getEmail()).orElseThrow(()-> new UsernameNotFoundException("User Not found"));
		
		
		var updateUser = UserApp.builder()
				.firstname(req.getFirstname())
				.lastname(req.getLastname())
				.email(req.getEmail())
				.build();
		
		repository.save(user);
	}*/
	
	public ResponseEntity<AuthenticationResponseDto> isLoggin(String header){
		
		try {
	        var jwtToken = jwtService.extractUsername(header);
	        if (jwtToken == null || jwtToken.isEmpty()) {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                    .body(AuthenticationResponseDto.builder()
	                            .message("Invalid token")
	                            .build());
	        }

	        var user = repository.findByEmail(jwtToken)
	                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

	        List<HikingSpot> userHikingSpots = user.getHikingSpots();
	        List<Spot> userSpots = user.getSpots();
	        return ResponseEntity.ok()
	                .body(AuthenticationResponseDto.builder()
	                        .message("success")
	                        .userApp(user)
	                        .userHikingSpots(userHikingSpots)
	                        .userSpots(userSpots)
	                        .build());
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(AuthenticationResponseDto.builder()
	                        .message("Error processing request: " + e.getMessage())
	                        .build());
	    }
	}
}
