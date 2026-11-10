package com.torneos.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.torneos.support.MutableClock;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

class JwtServiceTest {

    private static final String SECRET = "una-clave-secreta-de-prueba-con-mas-de-32-bytes";
    private static final long TTL_SECONDS = 900;

    private final MutableClock clock = new MutableClock(Instant.parse("2026-11-10T10:00:00Z"));
    private final JwtService service = new JwtService(SECRET, TTL_SECONDS, clock);

    @Test
    void generaYValidaTokenDeAdministrador() {
        String token = service.generateToken("admin", Role.ADMIN);

        assertThat(service.validate(token)).contains(new TokenClaims("admin", Role.ADMIN));
    }

    @Test
    void conservaElRolUser() {
        String token = service.generateToken("ana", Role.USER);

        assertThat(service.validate(token)).contains(new TokenClaims("ana", Role.USER));
    }

    @Test
    void rechazaTokenVencido() {
        String token = service.generateToken("ana", Role.USER);
        clock.advance(Duration.ofSeconds(TTL_SECONDS + 1));

        assertThat(service.validate(token)).isEmpty();
    }

    @Test
    void aceptaTokenJustoAntesDeVencer() {
        String token = service.generateToken("ana", Role.USER);
        clock.advance(Duration.ofSeconds(TTL_SECONDS - 1));

        assertThat(service.validate(token)).isPresent();
    }

    @Test
    void rechazaTokenConPayloadAlterado() {
        String userToken = service.generateToken("ana", Role.USER);
        String adminToken = service.generateToken("admin", Role.ADMIN);
        String[] user = userToken.split("\\.");
        String[] admin = adminToken.split("\\.");
        // Cabecera y firma de "ana" con el payload de "admin": un intento de escalar privilegios
        String forged = user[0] + "." + admin[1] + "." + user[2];

        assertThat(service.validate(forged)).isEmpty();
    }

    @Test
    void rechazaTokenFirmadoConOtraClave() {
        JwtService other = new JwtService("otra-clave-distinta-de-prueba-con-32-bytes!!", TTL_SECONDS, clock);
        String foreign = other.generateToken("admin", Role.ADMIN);

        assertThat(service.validate(foreign)).isEmpty();
    }

    @Test
    void rechazaTextosQueNoSonJwt() {
        Optional<TokenClaims> garbage = service.validate("esto-no-es-un-jwt");
        Optional<TokenClaims> empty = service.validate("");

        assertThat(garbage).isEmpty();
        assertThat(empty).isEmpty();
    }

    @Test
    void rechazaTokenFirmadoSinClaimDeRol() {
        String sinRol = signedToken(Jwts.builder().subject("ana"));

        assertThat(service.validate(sinRol)).isEmpty();
    }

    @Test
    void rechazaTokenConRolDesconocido() {
        String rolFalso = signedToken(Jwts.builder().subject("ana").claim("role", "SUPERADMIN"));

        assertThat(service.validate(rolFalso)).isEmpty();
    }

    /** Firma con la clave correcta un token que el servicio nunca generaria (sin rol o con rol falso). */
    private String signedToken(JwtBuilder builder) {
        return builder
                .expiration(Date.from(clock.instant().plusSeconds(60)))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    @Test
    void rechazaSecretoMenorA32Bytes() {
        String shortSecret = "a".repeat(31);

        assertThatThrownBy(() -> new JwtService(shortSecret, TTL_SECONDS, clock))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32 bytes");
    }

    @Test
    void aceptaSecretoDeExactamente32Bytes() {
        JwtService exact = new JwtService("a".repeat(32), TTL_SECONDS, clock);

        assertThat(exact.validate(exact.generateToken("ana", Role.USER))).isPresent();
    }

    @Test
    void exponeLaDuracionConfigurada() {
        assertThat(service.expirationSeconds()).isEqualTo(TTL_SECONDS);
    }
}
