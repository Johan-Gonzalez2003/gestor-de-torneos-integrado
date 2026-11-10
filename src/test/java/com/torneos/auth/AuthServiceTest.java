package com.torneos.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.torneos.common.AccountLockedException;
import com.torneos.common.InvalidCredentialsException;
import com.torneos.security.JwtService;
import com.torneos.security.LoginAttemptService;
import com.torneos.security.Role;
import com.torneos.security.TokenClaims;
import com.torneos.security.UserAccount;
import com.torneos.security.UserRepository;
import com.torneos.support.MutableClock;

class AuthServiceTest {

    private static final String PASSWORD = "Clave-De-Prueba-123";

    private final MutableClock clock = new MutableClock(Instant.parse("2026-11-10T10:00:00Z"));
    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private final JwtService jwtService = new JwtService("clave-de-prueba-con-mas-de-32-bytes-0123456789", 900, clock);
    private final UserRepository users = username -> "ana".equals(username)
            ? Optional.of(new UserAccount("ana", encoder.encode(PASSWORD), Role.USER))
            : Optional.empty();
    private final AuthService service =
            new AuthService(users, encoder, jwtService, new LoginAttemptService(2, 15, clock));

    @Test
    void loginCorrectoEntregaUnTokenValido() {
        LoginResponse response = service.login(new LoginRequest("ana", PASSWORD));

        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresInSeconds()).isEqualTo(900);
        assertThat(jwtService.validate(response.token())).contains(new TokenClaims("ana", Role.USER));
    }

    @Test
    void claveIncorrectaEsRechazada() {
        LoginRequest request = new LoginRequest("ana", "otra-clave");

        assertThatThrownBy(() -> service.login(request)).isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void usuarioInexistenteRecibeElMismoError() {
        LoginRequest request = new LoginRequest("fantasma", PASSWORD);

        assertThatThrownBy(() -> service.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Credenciales invalidas");
    }

    @Test
    void seBloqueaTrasLosIntentosFallidosPermitidos() {
        LoginRequest bad = new LoginRequest("ana", "mala");
        LoginRequest good = new LoginRequest("ana", PASSWORD);
        assertThatThrownBy(() -> service.login(bad)).isInstanceOf(InvalidCredentialsException.class);
        assertThatThrownBy(() -> service.login(bad)).isInstanceOf(InvalidCredentialsException.class);

        // Aun con la clave correcta, la cuenta sigue bloqueada
        assertThatThrownBy(() -> service.login(good)).isInstanceOf(AccountLockedException.class);
    }

    @Test
    void unLoginExitosoReiniciaLosFallos() {
        LoginRequest bad = new LoginRequest("ana", "mala");
        LoginRequest good = new LoginRequest("ana", PASSWORD);
        assertThatThrownBy(() -> service.login(bad)).isInstanceOf(InvalidCredentialsException.class);
        service.login(good);
        assertThatThrownBy(() -> service.login(bad)).isInstanceOf(InvalidCredentialsException.class);

        assertThatCode(() -> service.login(good)).doesNotThrowAnyException();
    }
}
