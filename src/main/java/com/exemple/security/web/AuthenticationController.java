package com.exemple.security.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.exemple.security.dtos.AuthenticationRequestDto;
import com.exemple.security.dtos.RegisterRequestDto;
import com.exemple.security.services.AuthenticationService;

import lombok.RequiredArgsConstructor;
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

	private final AuthenticationService service;
	
	@PostMapping("/register")
	public ResponseEntity<?> register(@RequestBody RegisterRequestDto request) {
		return service.register(request);
	}

	@PostMapping("/authenticate")
	public ResponseEntity<?> authenticate(@RequestBody AuthenticationRequestDto request) {
		return service.authenticate(request);
	}

	@GetMapping("/validate")
	public ResponseEntity<?> validate(){
		try {
			return ResponseEntity.ok(true);
		} catch (Exception e) {
			// TODO: handle exception
			return ResponseEntity.ok(false);
		}
	}
	
	@GetMapping("/users")
	public ResponseEntity<?> getAllUsers(){
		return service.getAllUsers();
	}
	
	@GetMapping("/user/get/{id}")
	public ResponseEntity<?> getUserById(@PathVariable int id){
		return service.getUserById(id);
	}
	
	@DeleteMapping("/delete/{id}")
	public ResponseEntity<?> deleteUser(@PathVariable int id){
		return service.delete(id);
	}
	
	
	@GetMapping("/user")
	public ResponseEntity<?> user(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
	    if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token manquant");
	    }
	    String token = authorizationHeader.replace("Bearer ", "");
	    return service.isLoggin(token);
	}

}
