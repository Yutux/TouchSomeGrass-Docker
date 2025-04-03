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
	@Value("${spring.jwt.privateKey}")
	public String secretKey;

    public boolean CheckToken(final String token) {
        try {
        	Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token);
        	return true;
        }catch(JwtException e) {
        	return false;
        }
    }
    
    public Claims getAllClaimsFromToken(String token) {
    	return Jwts.parserBuilder().setSigningKey(token).build().parseClaimsJws(token).getBody();
    }
    
    public boolean isTokenValid(String token) {
    	try {
			return this.getAllClaimsFromToken(token).getExpiration().before(new Date());
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
    }
    
    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
