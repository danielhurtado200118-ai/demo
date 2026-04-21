package com.example.demo;

import java.security.Key;
import java.util.Date;

import org.springframework.stereotype.Component; // Importante para la versión nueva

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
    // Generamos una llave segura automáticamente para la versión 0.11.5
    private Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    public String generateToken(String username, String rol) {
        return Jwts.builder()
                .setSubject(username)
                .claim("rol", rol)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                .signWith(key) // Usamos la nueva forma de firmar
                .compact();
    }
}

    

