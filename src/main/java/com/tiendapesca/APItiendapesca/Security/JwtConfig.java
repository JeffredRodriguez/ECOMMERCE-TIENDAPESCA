package com.tiendapesca.APItiendapesca.Security;

import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.security.Keys;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Configuration
public class JwtConfig {

    private final Dotenv dotenv;

    // Usar @Lazy para romper el ciclo de dependencias
    public JwtConfig(@Lazy Dotenv dotenv) {
        this.dotenv = dotenv;
    }
    
    @Bean
    public SecretKey jwtSecretKey() {
        String secretKey = dotenv.get("JWT_SECRET");
        if (secretKey == null || secretKey.trim().isEmpty()) {
            throw new IllegalArgumentException("La clave secreta JWT no puede estar vacía");
        }
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }
}