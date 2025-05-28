package com.tiendapesca.APItiendapesca.Security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 
 * - Generación de tokens JWT firmados
 * - Validación de tokens existentes
 * - Extracción de información de claims del token
 * 
 */
@Component 
public class JWT_TokenUtil {

   
    private final SecretKey SECRET_KEY = Keys.hmacShaKeyFor("una_clave_secreta_muy_larga_y_segura_32".getBytes());
    

    private final long EXPIRATION_TIME = 1000 * 60 * 60 * 10; 

    
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>(); 
        
        return Jwts.builder()
                .setClaims(claims) 
                .setSubject(username) 
                .setIssuedAt(new Date()) 
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) 
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256) 
                .compact(); 
    }

    /**
     * Valida un token JWT contra los UserDetails
     * @param userDetails Detalles del usuario para comparar
     * @return true si el token es válido para el usuario y no ha expirado
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

   
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

   
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY) // Clave para verificar firma
                .build()
                .parseClaimsJws(token) // Parsea y valida
                .getBody(); // Obtiene los claims
    }

    /**
     * Verifica si el token ha expirado
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}