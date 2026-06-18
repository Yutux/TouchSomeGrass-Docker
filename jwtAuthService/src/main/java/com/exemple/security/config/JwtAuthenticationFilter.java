package com.exemple.security.config;

import java.io.IOException;
import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.exemple.security.services.AccountServiceImpl;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AccountServiceImpl accountService;

    // ✅ Liste des routes publiques (pas besoin de JWT)
    private static final List<String> PUBLIC_EXACT_PATHS = List.of(
        "/api/v1/auth/register",
        "/api/v1/auth/authenticate",
        "/api/v1/auth/validate",
        "/api/v1/auth/users"
    );

    private static final List<String> PUBLIC_PREFIX_PATHS = List.of(
        "/api/v1/auth/user/get/",
        "/api/v1/spots/get/",
        "/api/v1/spots/search",
        "/api/v1/hikingspot/get/",
        "/api/v1/hikingspot/search",
        "/api/v1/hikingspot/all",
        "/api/v1/uploads/",
        "/api/v1/groupSource/public",
        "/api/v1/groupSource/search",
        "/api/v1/groupSource/details/"
    );

    /**
     * Vérifie si la route est publique (ne nécessite pas de JWT)
     */
    private boolean isPublicPath(String path) {
        // Routes exactes
        if (PUBLIC_EXACT_PATHS.contains(path)) {
            return true;
        }
        
        // Routes avec préfixe
        for (String prefix : PUBLIC_PREFIX_PATHS) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }
        
        // Routes avec pattern (commentaires/ratings publics)
        if (path.matches("/api/v1/user-relations/spots/\\d+/comments") ||
            path.matches("/api/v1/user-relations/hiking-spots/\\d+/comments") ||
            path.matches("/api/v1/user-relations/spots/\\d+/rating") ||
            path.matches("/api/v1/user-relations/hiking-spots/\\d+/rating") ||
            path.matches("/api/v1/groups/\\d+")) {
            return true;
        }
        
        return false;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String path = request.getServletPath();
        final String method = request.getMethod();

        // 🔓 BYPASS DES REQUÊTES OPTIONS (CORS preflight)
        if ("OPTIONS".equalsIgnoreCase(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 🔓 BYPASS DES ROUTES PUBLIQUES UNIQUEMENT
        if (isPublicPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 🔐 À PARTIR D'ICI → ROUTES PROTÉGÉES (y compris /api/v1/auth/user)
        final String authHeader = request.getHeader("Authorization");

        // Pas de token → continuer (Spring Security retournera 401/403)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        final String userEmail = jwtService.extractUsername(jwt);

        // Authentification si nécessaire
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = accountService.loadUserByUsername(userEmail);

            if (jwtService.isTokenValid(jwt, userDetails)) {

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}