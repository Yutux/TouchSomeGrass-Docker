package com.example.demo.utils;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import io.netty.handler.codec.http.HttpMethod;

import java.util.*;
import java.util.function.Predicate;

@Component
public class RouteValidator {

    public static final List<String> openApiEndpoints= List.of(
        // AUTH
        "/api/v1/auth/register",
        "/api/v1/auth/authenticate",
        "/api/v1/auth/validate",
        "/api/v1/auth/user/get/**",
        "/api/v1/auth/users",
        "/api/v1/auth/users/**",

        // SPOTS PUBLIC
        "/api/v1/spots/get/**",
        "/api/v1/spots/search",

        // HIKING SPOTS PUBLIC
        "/api/v1/hikingspot/get/**",
        "/api/v1/hikingspot/search",

        // COMMENTS & RATINGS PUBLIC
        "/api/v1/user-relations/spots/*/comments",
        "/api/v1/user-relations/hiking-spots/*/comments",
        "/api/v1/user-relations/spots/*/rating",
        "/api/v1/user-relations/hiking-spots/*/rating",

        // SOCIALS
        //"/api/v1/user-relations/*/favorites/**",
        //"/api/v1/user-relations/*/friends",
        //"/api/v1/user-relations/*/groups",
        //"/api/v1/user-relations/*/comments/**",
        //"/api/v1/user-relations/*/messages/**",
        // GROUPS PUBLIC
        "/api/v1/user-relations/groups/public",
        "/api/v1/user-relations/groups/search",
        "/api/v1/user-relations/groups/*",

        // FILES
        "/api/v1/uploads/**",

        // WEBSOCKETS
        "/ws/**"
    );

    public Predicate<ServerHttpRequest> isSecured = request -> {
        // ✅ OPTIONS n'est JAMAIS sécurisé
        if (request.getMethod().equals(HttpMethod.OPTIONS)) {
            System.out.println("✅ OPTIONS autorisé pour: " + request.getURI().getPath());
            return false;
        }
        
        String path = request.getURI().getPath();
        
        // ===== WEBSOCKET =====
        if (path.startsWith("/ws/")) {
            System.out.println("✅ Route publique WebSocket: " + path);
            return false;
        }
        
        // ===== AUTH PUBLIC =====
        if (path.equals("/api/v1/auth/register")) {
            System.out.println("✅ Route publique: " + path);
            return false;
        }
        if (path.equals("/api/v1/auth/authenticate")) {
            System.out.println("✅ Route publique: " + path);
            return false;
        }
        if (path.equals("/api/v1/auth/validate")) {
            System.out.println("✅ Route publique: " + path);
            return false;
        }
        
        // ===== UTILISATEURS PUBLIC =====
        if (path.startsWith("/api/v1/auth/users")) {
            System.out.println("✅ Route publique: " + path);
            return false;
        }
        if (path.matches("/api/v1/auth/user/get/\\d+")) {
            System.out.println("✅ Route publique: " + path);
            return false;
        }
        
        // ===== SPOTS PUBLIC =====
        if (path.startsWith("/api/v1/spots/get/")) {
            System.out.println("✅ Route publique: " + path);
            return false;
        }
        if (path.equals("/api/v1/spots/search")) {
            System.out.println("✅ Route publique: " + path);
            return false;
        }
        
        // ===== HIKING SPOTS PUBLIC =====
        if (path.startsWith("/api/v1/hikingspot/get/")) {
            System.out.println("✅ Route publique: " + path);
            return false;
        }
        if (path.equals("/api/v1/hikingspot/search")) {
            System.out.println("✅ Route publique: " + path);
            return false;
        }
        
        // ===== COMMENTAIRES & RATINGS PUBLIC =====
        if (path.matches("/api/v1/user-relations/spots/\\d+/comments")) {
            System.out.println("✅ Route publique: " + path);
            return false;
        }
        if (path.matches("/api/v1/user-relations/hiking-spots/\\d+/comments")) {
            System.out.println("✅ Route publique: " + path);
            return false;
        }
        if (path.matches("/api/v1/user-relations/spots/\\d+/rating")) {
            System.out.println("✅ Route publique: " + path);
            return false;
        }
        if (path.matches("/api/v1/user-relations/hiking-spots/\\d+/rating")) {
            System.out.println("✅ Route publique: " + path);
            return false;
        }
        
        // ===== GROUPES PUBLIC =====
        if (path.startsWith("/api/v1/user-relations/groups/public")) {
            System.out.println("✅ Route publique: " + path);
            return false;
        }
        if (path.startsWith("/api/v1/user-relations/groups/search")) {
            System.out.println("✅ Route publique: " + path);
            return false;
        }

        if (path.matches("/api/v1/user-relations/groups/\\d+")) {
            System.out.println("✅ Route publique: " + path);
            return false;
        }
        
        // ===== FICHIERS =====
        if (path.startsWith("/api/v1/uploads/")) {
            System.out.println("✅ Route publique: " + path);
            return false;
        }
        
        // ✅ Si aucune route publique ne matche → Route SÉCURISÉE
        System.out.println("🔐 Route sécurisée: " + path);
        return true;
    };
}