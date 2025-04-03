package com.exemple.security.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.security.Principal;

import com.exemple.security.config.LoggedInUser;
import com.exemple.security.dtos.AuthenticationRequestDto;
import com.exemple.security.dtos.RegisterRequestDto;
import com.exemple.security.entities.UserApp;
import com.exemple.security.services.AuthenticationService;

import lombok.RequiredArgsConstructor;
@RestController
@RequestMapping("/api/v1/home")
@RequiredArgsConstructor
public class TestController {


	@GetMapping("/")
	public String sayHello() {
		return "hello";
	}

}
