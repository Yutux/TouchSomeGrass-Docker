package com.example.demo.utils;

import java.security.Key;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtService {
    
    @Value("${spring.jwt.securityKey}")
    private String secretKey;
    
    /**
     * Valide le token JWT
     * @param token Le token JWT à valider
     * @return true si le token est valide, false sinon
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSignKey())  // ✅ Utilise la clé secrète
                .build()
                .parseClaimsJws(token);
            
            // Vérifier également l'expiration
            return !isTokenExpired(token);
            
        } catch (JwtException e) {
            System.err.println("Invalid JWT token: " + e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            System.err.println("JWT token is empty: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Récupère tous les claims du token
     * @param token Le token JWT
     * @return Les claims extraits du token
     */
    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())  // ✅ FIX: Utilise la clé secrète, pas le token
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    /**
     * Vérifie si le token est expiré
     * @param token Le token JWT
     * @return true si le token est expiré, false sinon
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getAllClaimsFromToken(token).getExpiration();
            return expiration.before(new Date());  // ✅ FIX: before() = expiré
        } catch (Exception e) {
            System.err.println("Error checking token expiration: " + e.getMessage());
            return true;  // En cas d'erreur, considérer comme expiré
        }
    }
    
    /**
     * Extrait le username (email) du token
     * @param token Le token JWT
     * @return Le username/email contenu dans le token
     */
    public String extractUsername(String token) {
        return getAllClaimsFromToken(token).getSubject();
    }
    
    /**
     * Génère la clé de signature à partir du secret
     * @return La clé HMAC
     */
    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}