package com.exemple.security.config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


import com.exemple.security.services.AccountServiceImpl;
import com.exemple.security.tools.CustomPasswordEncoder;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfiguration{
	
	@Autowired
	private final JwtAuthenticationFilter jwtAuthFilter;
	@Autowired
	private final CustomPasswordEncoder passwordEncoder;
	@Autowired
	private AccountServiceImpl accountService;
	@Autowired
	private LogoutService logoutHandler;
	
	private static final String[] WHITE_LIST_URL = {
			"/**",
			"/api/v1/auth/**",
			"/api/v1/auth/user",
			"/api/v1/auth/users",
			"/api/v1/auth/user/**",
			"/api/v1/auth/user/{id}",
			"/api/v1/hikingspot/all",
			"/api/v1/hikingspot/get/**",
			"/api/v1/spots/get/**",
			"/api/v1/spots",
			"/api/v1/uploads",
			"/api/v1/uploads/**",
			"/api/v1/spots/search",
			"/api/v1/hikingspot/search"
	};
	
	
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		
		http
		.csrf(csrf -> csrf 
			      .disable()
			    )
		.authorizeHttpRequests(req ->req
				.requestMatchers(WHITE_LIST_URL).permitAll()
				.requestMatchers(HttpMethod.POST, "/api/v1/hikingspot/create*").authenticated()
		        .requestMatchers(HttpMethod.DELETE, "/api/v1/hikingspot/**").authenticated()
		        .requestMatchers(HttpMethod.POST, "/api/v1/spots/create").authenticated()
		        .requestMatchers(HttpMethod.DELETE, "/api/v1/spots/delete/**").authenticated()
		        //.requestMatchers(HttpMethod.POST, "/api/v1/spots/create").authenticated()
		        .requestMatchers(HttpMethod.DELETE, "/api/v1/auth/delete/**").authenticated()
				.anyRequest().authenticated()
		).logout(logout ->
        logout.logoutUrl("/api/v1/auth/logout")
                .addLogoutHandler(logoutHandler)
                .logoutSuccessHandler((request, response, authentication) 
                		-> SecurityContextHolder.clearContext()))
		/*.csrf((csrf) -> csrf
				.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
				//.csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
		)*/
		.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
		.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
		;
	return http.build();
	}
	
	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(accountService);
		authProvider.setPasswordEncoder(passwordEncoder);
		return authProvider;
	}
}
