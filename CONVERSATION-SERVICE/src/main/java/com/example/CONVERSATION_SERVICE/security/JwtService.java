package com.example.CONVERSATION_SERVICE.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import jakarta.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {

    private final JwtProperties jwtProperties;

    private SecretKey signingKey;

    @PostConstruct
    public void init() {

        byte[] keyBytes =
                Decoders.BASE64.decode(
                        jwtProperties.getSecret()
                );

        signingKey =
                Keys.hmacShaKeyFor(
                        keyBytes
                );
    }

    private Claims extractAllClaims(
            String token
    ) {

        return Jwts
                .parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(
            String token
    ) {

        return extractAllClaims(token)
                .getSubject();
    }

    public Long extractUserId(
            String token
    ) {

        return extractAllClaims(token)
                .get(
                        "userId",
                        Long.class
                );
    }

    public String extractRole(
            String token
    ) {

        return extractAllClaims(token)
                .get(
                        "role",
                        String.class
                );
    }

    public String extractTokenType(
            String token
    ) {

        return extractAllClaims(token)
                .get(
                        "tokenType",
                        String.class
                );
    }

    public Date extractExpiration(
            String token
    ) {

        return extractAllClaims(token)
                .getExpiration();
    }

    public boolean isTokenNotExpired(
            String token
    ) {

        Date expiration =
                extractExpiration(token);

        return expiration != null
                && expiration.after(
                new Date()
        );
    }

    public boolean isAccessToken(
            String token
    ) {

        return "ACCESS".equals(
                extractTokenType(token)
        );
    }

    public boolean validateAccessToken(
            String token
    ) {

        try {

            return isTokenNotExpired(token)
                    && isAccessToken(token);

        } catch (Exception ex) {

            log.warn(
                    "JWT access-token validation failed",
                    ex
            );

            return false;
        }
    }

    public AuthenticatedUser buildAuthenticatedUser(
            String token
    ) {

        Claims claims =
                extractAllClaims(token);

        Long userId =
                claims.get(
                        "userId",
                        Long.class
                );

        String email =
                claims.getSubject();

        String role =
                claims.get(
                        "role",
                        String.class
                );

        Date expiration =
                claims.getExpiration();

        return new AuthenticatedUser(
                userId,
                email,
                role,
                expiration.toInstant()
        );
    }
}