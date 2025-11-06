package com.example.mediplan.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    private final SecretKey accessKey;
    private final SecretKey refreshKey;
    private final long accessExpMin;
    private final long refreshExpDays;

    public JwtService(
            @Value("${app.security.jwt.access-secret}") String accessSecret,
            @Value("${app.security.jwt.refresh-secret}") String refreshSecret,
            @Value("${app.security.jwt.access-expiration-min}") long accessExpMin,
            @Value("${app.security.jwt.refresh-expiration-days}") long refreshExpDays
    ) {
        this.accessKey = Keys.hmacShaKeyFor(accessSecret.getBytes(StandardCharsets.UTF_8));
        this.refreshKey = Keys.hmacShaKeyFor(refreshSecret.getBytes(StandardCharsets.UTF_8));
        this.accessExpMin = accessExpMin;
        this.refreshExpDays = refreshExpDays;
    }

    public String generateAccessToken(String userId, String email, String role) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(accessExpMin * 60);
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        if (email != null) {
            claims.put("email", email);
        }
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .addClaims(claims)
                .signWith(accessKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(String userId) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(refreshExpDays * 86400);
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(refreshKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> parseAccessToken(String token) {
        return Jwts.parserBuilder().setSigningKey(accessKey).build().parseClaimsJws(token);
    }

    public Jws<Claims> parseRefreshToken(String token) {
        return Jwts.parserBuilder().setSigningKey(refreshKey).build().parseClaimsJws(token);
    }

    public long getAccessExpMin() {
        return accessExpMin;
    }

    public long getRefreshExpDays() {
        return refreshExpDays;
    }
}
