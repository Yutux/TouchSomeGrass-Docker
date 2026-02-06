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

        // ✅ GROUPS PUBLIC - NOUVEAU CHEMIN groupSource
        "/api/v1/groupSource/public",
        "/api/v1/groupSource/search",
        "/api/v1/groupSource/details/**",

        // FILES
        "/api/v1/uploads/**",

        // WEBSOCKETS
        "/ws/**"
    );

    public Predicate<ServerHttpRequest> isSecured = request -> {
        // ✅ OPTIONS n'est JAMAIS sécurisé
        if (request.getMethod() != null && request.getMethod().equals(HttpMethod.OPTIONS)) {
            return false;
        }
        
        String path = request.getURI().getPath();
        String method = request.getMethod() != null ? request.getMethod().name() : "UNKNOWN";
        
        // ===== WEBSOCKET =====
        if (path.startsWith("/ws/")) {
            return false;
        }

        // 🔍 LOG
        System.out.println("🔒 RouteValidator checking: " + method + " " + path);
        
        // ✅ OPTIONS n'est JAMAIS sécurisé
        if (request.getMethod() != null && request.getMethod().equals(HttpMethod.OPTIONS)) {
            System.out.println("   → OPTIONS method - NOT SECURED");
            return false;
        }
        
        // ===== AUTH PUBLIC =====
        if (path.equals("/api/v1/auth/register")) {
            return false;
        }
        if (path.equals("/api/v1/auth/authenticate")) {
            return false;
        }
        if (path.equals("/api/v1/auth/validate")) {
            return false;
        }
        
        // ===== UTILISATEURS PUBLIC =====
        if (path.startsWith("/api/v1/auth/users")) {
            return false;
        }
        if (path.matches("/api/v1/auth/user/get/\\d+")) {
            return false;
        }
        
        // ===== SPOTS PUBLIC =====
        if (path.startsWith("/api/v1/spots/get/")) {
            return false;
        }
        if (path.equals("/api/v1/spots/search")) {
            return false;
        }
        
        // ===== HIKING SPOTS PUBLIC =====
        if (path.startsWith("/api/v1/hikingspot/get/")) {
            return false;
        }
        if (path.equals("/api/v1/hikingspot/search")) {
            return false;
        }
        
        // ===== COMMENTAIRES & RATINGS PUBLIC =====
        if (path.matches("/api/v1/user-relations/spots/\\d+/comments")) {
            return false;
        }
        if (path.matches("/api/v1/user-relations/hiking-spots/\\d+/comments")) {
            return false;
        }
        if (path.matches("/api/v1/user-relations/spots/\\d+/rating")) {
            return false;
        }
        if (path.matches("/api/v1/user-relations/hiking-spots/\\d+/rating")) {
            return false;
        }
        
        // ===== ✅ GROUPES PUBLIC - groupSource =====
        if (path.equals("/api/v1/groupSource/public")) {
            return false;
        }
        if (path.equals("/api/v1/groupSource/search") || 
            path.startsWith("/api/v1/groupSource/search?")) {
            return false;
        }
        if (path.startsWith("/api/v1/groupSource/details/")) {
            return false;
        }
        
        // ===== FICHIERS =====
        if (path.startsWith("/api/v1/uploads/")) {
            return false;
        }
        
        // ✅ Si aucune route publique ne matche → Route SÉCURISÉE
        System.out.println("   → No public match - SECURED");
        return true;
    };
}