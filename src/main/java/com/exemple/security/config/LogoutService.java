package com.exemple.security.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class LogoutService implements LogoutHandler {

	public void logout(
		    HttpServletRequest request,
		    HttpServletResponse response,
		    Authentication authentication
		) {
		  final String authHeader = request.getHeader("Authorization");
		  final String jwt;
		  if (authHeader == null ||!authHeader.startsWith("Bearer ")) {
		    return;
		  }
		  jwt = authHeader.substring(7);
		  if (jwt != null) {
			  System.out.println("logged out");
		  }
		}
}
