package com.torneos.security;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Crea y valida tokens JWT firmados con HMAC-SHA256.
 * El secreto viene de una variable de entorno y debe tener al menos 32 bytes.
 */
@Service
public class JwtService {

    static final int MIN_SECRET_BYTES = 32;
    private static final String ROLE_CLAIM = "role";

    private final SecretKey key;
    private final long expirationSeconds;
    private final Clock clock;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-seconds}") long expirationSeconds,
                      Clock clock) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException("JWT_SECRET debe tener al menos 32 bytes");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        this.expirationSeconds = expirationSeconds;
        this.clock = clock;
    }

    public long expirationSeconds() {
        return expirationSeconds;
    }

    public String generateToken(String username, Role role) {
        Instant now = clock.instant();
        return Jwts.builder()
                .subject(username)
                .claim(ROLE_CLAIM, role.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirationSeconds)))
                .signWith(key)
                .compact();
    }

    /** Devuelve los datos del token, o vacio si esta vencido, alterado o mal formado. */
    public Optional<TokenClaims> validate(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .clock(() -> Date.from(clock.instant()))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            String roleName = claims.get(ROLE_CLAIM, String.class);
            if (roleName == null) {
                return Optional.empty();
            }
            return Optional.of(new TokenClaims(claims.getSubject(), Role.valueOf(roleName)));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
