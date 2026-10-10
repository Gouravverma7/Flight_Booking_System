package com.flightbooking.auth.security;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration) {

        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException(
                    "jwt.secret must be at least 32 bytes long"
            );
        }

        this.secretKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );

        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public String generateAccessToken(
            Long userId,
            String email,
            String role) {

        return generateToken(
                userId,
                email,
                role,
                "ACCESS",
                accessTokenExpiration
        );
    }

    public String generateRefreshToken(
            Long userId,
            String email) {

        return generateToken(
                userId,
                email,
                null,
                "REFRESH",
                refreshTokenExpiration
        );
    }

    private String generateToken(
            Long userId,
            String email,
            String role,
            String tokenType,
            long expiration) {

        Date now = new Date();
        Date expiry = new Date(
                now.getTime() + expiration
        );

        var builder = Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("tokenType", tokenType)
                .issuedAt(now)
                .expiration(expiry);

        if (role != null) {
            builder.claim("role", role);
        }

        return builder
                .signWith(secretKey)
                .compact();
    }

    public Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Long extractUserId(String token) {
        return extractAllClaims(token)
                .get("userId", Long.class);
    }

    public String extractRole(String token) {
        return extractAllClaims(token)
                .get("role", String.class);
    }

    public String extractTokenType(String token) {
        return extractAllClaims(token)
                .get("tokenType", String.class);
    }

    public boolean isTokenValid(String token) {

        try {
            Claims claims = extractAllClaims(token);

            return claims.getExpiration().after(new Date());

        } catch (Exception e) {
            return false;
        }
    }

    public boolean isAccessTokenValid(String token) {

        try {
            Claims claims = extractAllClaims(token);

            return "ACCESS".equals(
                    claims.get("tokenType", String.class)
            )
                    && claims.getExpiration().after(new Date());

        } catch (Exception e) {
            return false;
        }
    }

    public boolean isRefreshTokenValid(String token) {

        try {
            Claims claims = extractAllClaims(token);

            return "REFRESH".equals(
                    claims.get("tokenType", String.class)
            )
                    && claims.getExpiration().after(new Date());

        } catch (Exception e) {
            return false;
        }
    }

    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    public Date getRefreshTokenExpirationDate() {

        return new Date(
                System.currentTimeMillis()
                        + refreshTokenExpiration
        );
    }






















}
