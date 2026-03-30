package com.examportal.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

/**
 * JwtUtil — generates and validates JWTs.
 *
 * Key concepts used here (great for interviews):
 *  - HMAC-SHA256 signing: symmetric key, fast, secure for internal APIs
 *  - jti (JWT ID): unique UUID per token — enables revocation auditing
 *  - Subject: stores username — stateless identity lookup
 *  - Expiry: 15 minutes for access tokens — limits blast radius if stolen
 */
@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long accessTokenExpiryMs;

    public JwtUtil(
            @Value("${app.jwt.secret}") String base64Secret,
            @Value("${app.jwt.access-token-expiry-ms}") long accessTokenExpiryMs) {

        byte[] keyBytes = Base64.getDecoder().decode(base64Secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpiryMs = accessTokenExpiryMs;
    }

    public String generateAccessToken(UserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("roles", userDetails.getAuthorities().toString())
                .id(UUID.randomUUID().toString())          // jti claim
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiryMs))
                .signWith(secretKey)
                .compact();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private boolean isExpired(String token) {
        return parseClaims(token).getExpiration().before(new Date());
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
