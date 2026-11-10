package com.torneos.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.torneos.support.MutableClock;

class LoginAttemptServiceTest {

    private final MutableClock clock = new MutableClock(Instant.parse("2026-11-10T10:00:00Z"));
    private final LoginAttemptService service = new LoginAttemptService(3, 15, clock);

    @Test
    void usuarioNuevoNoEstaBloqueado() {
        assertThat(service.isLocked("ana")).isFalse();
    }

    @Test
    void seBloqueaAlAlcanzarElMaximoDeFallos() {
        service.registerFailure("ana");
        service.registerFailure("ana");
        assertThat(service.isLocked("ana")).isFalse();

        service.registerFailure("ana");
        assertThat(service.isLocked("ana")).isTrue();
    }

    @Test
    void sigueBloqueadoAntesDeCumplirseElTiempo() {
        lock("ana");
        clock.advance(Duration.ofMinutes(15).minusSeconds(1));

        assertThat(service.isLocked("ana")).isTrue();
    }

    @Test
    void seDesbloqueaCuandoPasaElTiempo() {
        lock("ana");
        clock.advance(Duration.ofMinutes(15));

        assertThat(service.isLocked("ana")).isFalse();
    }

    @Test
    void trasDesbloquearseUnSoloFalloNoVuelveABloquear() {
        lock("ana");
        clock.advance(Duration.ofMinutes(15));

        service.registerFailure("ana");

        assertThat(service.isLocked("ana")).isFalse();
    }

    @Test
    void trasDesbloquearseSeBloqueaDeNuevoAlTercerFallo() {
        lock("ana");
        clock.advance(Duration.ofMinutes(15));

        lock("ana");

        assertThat(service.isLocked("ana")).isTrue();
    }

    @Test
    void unLoginExitosoReiniciaElContador() {
        service.registerFailure("ana");
        service.registerFailure("ana");
        service.registerSuccess("ana");
        service.registerFailure("ana");
        service.registerFailure("ana");

        assertThat(service.isLocked("ana")).isFalse();
    }

    @Test
    void cadaUsuarioTieneSuPropioContador() {
        lock("ana");

        assertThat(service.isLocked("ana")).isTrue();
        assertThat(service.isLocked("luis")).isFalse();
    }

    private void lock(String username) {
        for (int i = 0; i < 3; i++) {
            service.registerFailure(username);
        }
    }
}
