package com.fintrack.budget_service.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret:your-default-secret-key-change-this-in-production}")
    private String secret;

    // Extract user ID from token
    public Long extractUserId(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Object userIdObj = claims.get("userId");

            if (userIdObj instanceof Integer) {
                return ((Integer) userIdObj).longValue();
            } else if (userIdObj instanceof Long) {
                return (Long) userIdObj;
            } else if (userIdObj != null) {
                return Long.parseLong(userIdObj.toString());
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    // Extract username from token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extract expiration date from token
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Extract specific claim
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Extract all claims
    private Claims extractAllClaims(String token) {
        // Simplified parsing without signature verification for now
        // In production, you should verify the signature
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new RuntimeException("Invalid JWT token");
        }

        // Just parse the payload (middle part)
        String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));

        // Create a simple parser to extract claims
        return Jwts.parser()
                .build()
                .parseClaimsJwt(token)
                .getPayload();
    }

    // Check if token is expired
    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Validate token
    public Boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}