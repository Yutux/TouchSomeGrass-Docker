package com.exemple.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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
    
    private static final String[] WHITE_LIST_URL = {
        // ===== AUTH PUBLIC =====
        "/api/v1/auth/register",
        "/api/v1/auth/authenticate",
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
        
        // ===== GROUPES (LECTURE PUBLIQUE) =====
        "/api/v1/user-relations/groups/public",
        "/api/v1/user-relations/groups/search",
        "/api/v1/user-relations/groups/*",  // GET /groups/{id}
        
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
            
            .authorizeHttpRequests(req -> req
                // ========================================
                // 🔓 ROUTES PUBLIQUES
                // ========================================
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(WHITE_LIST_URL).permitAll()
                
                // ========================================
                // 🔒 ROUTES PRIVÉES
                // ========================================
                
                // ===== AUTH PRIVÉ =====
                .requestMatchers("/api/v1/auth/user").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/v1/auth/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/v1/auth/delete/**").authenticated()
                
                // ===== SPOTS =====
                .requestMatchers(HttpMethod.POST, "/api/v1/spots/create").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/v1/spots/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/v1/spots/delete/**").authenticated()
                
                // ===== HIKING SPOTS =====
                .requestMatchers(HttpMethod.POST, "/api/v1/hikingspot/create*").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/v1/hikingspot/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/v1/hikingspot/**").authenticated()
                
                // ===== FAVORIS =====
                .requestMatchers("/api/v1/user-relations/**").authenticated()
                .requestMatchers("/api/v1/user-relations/favorites/**").authenticated()
                
                // ===== AMIS =====
                .requestMatchers("/api/v1/user-relations/friends").authenticated()
                .requestMatchers("/api/v1/user-relations/friends/**").authenticated()
                
                // ===== MESSAGES =====
                .requestMatchers("/api/v1/user-relations/messages/**").authenticated()
                
                // ===== COMMENTAIRES PRIVÉS =====
                .requestMatchers(HttpMethod.POST, "/api/v1/user-relations/comments").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/v1/user-relations/comments/reply").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/v1/user-relations/comments").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/v1/user-relations/comments/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/v1/user-relations/comments/**").authenticated()
                
                // ===== GROUPES - GESTION =====
                .requestMatchers(HttpMethod.POST, "/api/v1/user-relations/groups").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/v1/user-relations/groups").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/v1/user-relations/groups/*").authenticated()
                
                // ===== GROUPES - MEMBRES =====
                .requestMatchers(HttpMethod.GET, "/api/v1/user-relations/groups/*/members").authenticated()
                .requestMatchers("/api/v1/user-relations/groups/*/members/**").authenticated()
                
                // ===== GROUPES - CONVERSATIONS =====
                .requestMatchers("/api/v1/user-relations/groups/*/conversations").authenticated()
                .requestMatchers("/api/v1/user-relations/groups/*/leave").authenticated()
                
                // ===== INVITATIONS =====
                .requestMatchers("/api/v1/user-relations/groups/*/invitations").authenticated()
                .requestMatchers("/api/v1/user-relations/groups/*/invitations/**").authenticated()
                .requestMatchers("/api/v1/user-relations/groups/invitations/**").authenticated()
                .requestMatchers("/api/v1/friend-requests/**").authenticated()
                
                // ===== CONVERSATIONS =====
                .requestMatchers("/api/v1/user-relations/conversations").authenticated()
                .requestMatchers("/api/v1/user-relations/conversations/**").authenticated()
                
                // ===== TOUT LE RESTE =====
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