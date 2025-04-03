package com.example.demo.filter;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
/*
@Configuration
public class CorsConfig {

	@Bean
    CorsWebFilter corsFiltercustom() {
        return new CorsWebFilter(corsConfigurationSourceCustom());
	}
	@Bean
    CorsConfigurationSource corsConfigurationSourceCustom() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration().applyPermitDefaultValues();
        config.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:8761", "http://localhost:8080", "http://localhost:8088"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS", "HEAD"));
        config.setAllowCredentials(true);
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setMaxAge(3600L);
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
*/