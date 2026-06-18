package com.example.demo.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.example.demo.utils.JwtService;
import com.example.demo.utils.RouteValidator;

import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    @Autowired
    private RouteValidator validator;

    @Autowired
    private JwtService jwtService;

    public JwtAuthenticationFilter() {
        super(Config.class);
    }

   /* @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            
            if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
                return chain.filter(exchange);
            }

            if (!validator.isSecured.test(exchange.getRequest())) {
                System.out.println("✅ Route publique: " + exchange.getRequest().getPath());
                return chain.filter(exchange);
            }

            System.out.println("🔐 Route sécurisée: " + exchange.getRequest().getPath());

            if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                System.out.println("❌ Pas de header Authorization");
                return onError(exchange, "Missing Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            System.out.println("🔑 Token reçu: " + authHeader.substring(0, 30) + "...");
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                System.out.println("❌ Format invalide");
                return onError(exchange, "Invalid authorization header format", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);
            try {
                boolean isValid = jwtService.validateToken(token);
                System.out.println("🔍 Token valide ? " + isValid);
                
                if (!isValid) {
                    return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
                }
                
                System.out.println("✅ Token accepté, passage au service");
                
            } catch (Exception e) {
                System.out.println("❌ Erreur validation: " + e.getMessage());
                e.printStackTrace();
                return onError(exchange, "Unauthorized access: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
            }

            return chain.filter(exchange);
        };
    }
*/
    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        exchange.getResponse().setStatusCode(httpStatus);
        return exchange.getResponse().setComplete();
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            var request = exchange.getRequest();
            
            // 🔍 LOG 1 : Requête entrante
            System.out.println("===========================================");
            System.out.println("🌐 GATEWAY - Request received:");
            System.out.println("   Method: " + request.getMethod());
            System.out.println("   Path: " + request.getURI().getPath());
            System.out.println("   Full URI: " + request.getURI());
            
            // 🔍 LOG 2 : Check si route sécurisée
            boolean isSecured = validator.isSecured.test(request);
            System.out.println("   Is Secured: " + isSecured);
            
            if (!isSecured) {
                System.out.println("   ✅ PUBLIC ROUTE - Passing through");
                System.out.println("===========================================");
                return chain.filter(exchange);
            }
            
            // 🔍 LOG 3 : Vérification du header Authorization
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                System.out.println("   ❌ MISSING Authorization header");
                System.out.println("===========================================");
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String authHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            System.out.println("   Authorization header: " + authHeader.substring(0, Math.min(20, authHeader.length())) + "...");
            
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                authHeader = authHeader.substring(7);
                
                // 🔍 LOG 4 : Validation JWT
                try {
                    System.out.println("   🔑 Validating JWT token...");
                    jwtService.validateToken(authHeader);
                    System.out.println("   ✅ JWT VALID - Routing to service");
                    System.out.println("===========================================");
                } catch (Exception e) {
                    System.out.println("   ❌ JWT INVALID: " + e.getMessage());
                    System.out.println("===========================================");
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
            } else {
                System.out.println("   ❌ Invalid Authorization header format");
                System.out.println("===========================================");
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
            
            return chain.filter(exchange);
        };
    }

    public static class Config {
    }
}