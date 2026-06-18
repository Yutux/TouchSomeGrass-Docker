package com.exemple.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import com.exemple.security.services.AccountServiceImpl;
import com.exemple.security.tools.CustomPasswordEncoder;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfiguration {
    
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CustomPasswordEncoder passwordEncoder;
    private final AccountServiceImpl accountService;
    private final LogoutService logoutHandler;
    private final CorsConfigurationSource corsConfigurationSource;
    
    private static final String[] WHITE_LIST_URL = {
        // ===== AUTH PUBLIC =====
        "/api/v1/auth/register",
        //"/api/v1/auth/authenticate",
        "/api/v1/auth/validate",
        
        // ===== UTILISATEURS (LECTURE PUBLIQUE) =====
        "/api/v1/auth/users",
        "/api/v1/auth/user/{id}",
        "/api/v1/auth/user/get/**",
        
        // ===== SPOTS (LECTURE PUBLIQUE) =====
        "/api/v1/spots",
        "/api/v1/spots/get/**",
        "/api/v1/spots/search",
        
        // ===== HIKING SPOTS (LECTURE PUBLIQUE) =====
        "/api/v1/hikingspot/all",
        "/api/v1/hikingspot/get/**",
        "/api/v1/hikingspot/search",
        
        // ===== COMMENTAIRES & RATINGS (LECTURE PUBLIQUE) =====
        "/api/v1/user-relations/spots/*/comments",
        "/api/v1/user-relations/hiking-spots/*/comments",
        "/api/v1/user-relations/spots/*/rating",
        "/api/v1/user-relations/hiking-spots/*/rating",
        
        // ===== FICHIERS =====
        "/api/v1/uploads",
        "/api/v1/uploads/**",
    };

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
            .requestMatchers("/ws/**");
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(req -> req
                // ========================================
                // 🔓 ROUTES PUBLIQUES (EN PREMIER !)
                // ========================================
                
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/register").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/authenticate").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/validate").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/groupSource/public").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/groupSource/search").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/groupSource/details/**").permitAll()
                .requestMatchers(WHITE_LIST_URL).permitAll()
                
                // ========================================
                // 🔒 ROUTES PRIVÉES
                // ========================================
                
                .requestMatchers("/api/v1/auth/user").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/v1/auth/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/v1/auth/delete/**").authenticated()
                
                .requestMatchers(HttpMethod.POST, "/api/v1/spots/create").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/v1/spots/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/v1/spots/delete/**").authenticated()
                
                .requestMatchers(HttpMethod.POST, "/api/v1/hikingspot/create*").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/v1/hikingspot/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/v1/hikingspot/**").authenticated()
                
                .requestMatchers(HttpMethod.POST, "/api/v1/user-relations/comments").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/v1/user-relations/comments/reply").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/v1/user-relations/comments").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/v1/user-relations/comments/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/v1/user-relations/comments/**").authenticated()
                
                .requestMatchers("/api/v1/user-relations/favorites/**").authenticated()
                .requestMatchers("/api/v1/user-relations/friends/**").authenticated()
                .requestMatchers("/api/v1/friend-requests/**").authenticated()
                .requestMatchers("/api/v1/user-relations/messages/**").authenticated()
                
                // ===== CONVERSATIONS - CORRIGÉ =====
                .requestMatchers(HttpMethod.POST, "/api/v1/user-relations/conversations/private").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/v1/user-relations/conversations/group").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/v1/user-relations/conversations/messages").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/v1/user-relations/groups/*/conversations").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/v1/user-relations/groups/*/conversations").authenticated()
                .requestMatchers("/api/v1/user-relations/conversations/**").authenticated()
                
                .requestMatchers(HttpMethod.POST, "/api/v1/groupSource/create").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/v1/groupSource/my-groups").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/v1/groupSource/delete/**").authenticated()
                .requestMatchers("/api/v1/groupSource/*/members/**").authenticated()
                .requestMatchers("/api/v1/groupSource/*/leave").authenticated()
                
                .anyRequest().authenticated()
            )
            
            .logout(logout -> logout
                .logoutUrl("/api/v1/auth/logout")
                .addLogoutHandler(logoutHandler)
                .logoutSuccessHandler((request, response, authentication) -> 
                    SecurityContextHolder.clearContext())
            )
            
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
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