package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.cloud.gateway.discovery.DiscoveryClientRouteDefinitionLocator;
import org.springframework.cloud.gateway.discovery.DiscoveryLocatorProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import com.example.demo.filter.JwtAuthenticationFilter;

import java.util.Arrays;

@SpringBootApplication
@EnableDiscoveryClient
public class CloudGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudGatewayApplication.class, args);
    }

    // ===== ROUTES DYNAMIQUES =====
    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder, JwtAuthenticationFilter customHeaderFilter) {
        return builder.routes()
                .route(r -> r.path("/api/v1/auth/**")
                        .filters(f -> f.filter(customHeaderFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://AUTH-SERVICE"))
                .route(r -> r.path("/api/v1/spots/**")
                        .filters(f -> f.filter(customHeaderFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://AUTH-SERVICE"))
                .route(r -> r.path("/api/v1/hikingspot/**")
                        .filters(f -> f.filter(customHeaderFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://AUTH-SERVICE"))
                .route("friend-requests", r -> r.path("/api/v1/friend-requests/**")
                        .uri("lb://AUTH-SERVICE"))
                .route("group-source", r -> r.path("/api/v1/groupSource/**")
                    .filters(f -> f.filter(customHeaderFilter.apply(new JwtAuthenticationFilter.Config())))
                    .uri("lb://AUTH-SERVICE"))
                .route(r -> r.path("/api/v1/user-relations/**")
                        .filters(f -> f.filter(customHeaderFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://AUTH-SERVICE"))
				.route(r -> r.path("/ws/**")
                    .uri("lb://AUTH-SERVICE"))
                .build();
    }

    @Bean
    public DiscoveryClientRouteDefinitionLocator dynamicRoutes(ReactiveDiscoveryClient rdc,
                                                              DiscoveryLocatorProperties dlp) {
        return new DiscoveryClientRouteDefinitionLocator(rdc, dlp);
    }

    // ===== CORS GLOBAL POUR GATEWAY (CORRECT) =====/* 
	@Bean
        public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:3000",
                "https://front-touchsome-grass.vercel.app",
                "https://front-touchsome-grass-lrfa1a4d4-yutuxs-projects.vercel.app",
                "https://touchsomegrass.fr"
        ));
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        corsConfig.setAllowedHeaders(Arrays.asList("*"));
        corsConfig.setAllowCredentials(true);
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
        }
}