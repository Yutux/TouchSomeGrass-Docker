package com.exemple.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // ✅ Autoriser localhost:3000
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "https://front-touchsome-grass-lrfa1a4d4-yutuxs-projects.vercel.app", "https://front-touchsome-grass.vercel.app/", "https://touchsomegrass.fr"));
        
        // ✅ Autoriser tous les headers
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // ✅ Autoriser toutes les méthodes (GET, POST, PUT, DELETE, OPTIONS)
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // ✅ Autoriser les credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        
        // ✅ Appliquer à tous les endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}