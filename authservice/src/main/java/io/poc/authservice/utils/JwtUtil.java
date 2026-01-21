package io.poc.authservice.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Duration expiration;

    public String generateJwt(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        return Jwts
                .builder()
                .setClaims(claims)
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration.toMillis()))
                .signWith(getKey())
                .compact();
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

}
